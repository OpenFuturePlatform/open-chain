package io.openfuture.chain.network.service

import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.core.sync.DefaultSyncBlockHandler
import io.openfuture.chain.network.component.node.NodeClock
import io.openfuture.chain.network.message.base.BaseMessage
import io.openfuture.chain.network.message.network.*
import io.openfuture.chain.network.message.network.HeartBeatMessage.Type.PING
import io.openfuture.chain.network.message.network.HeartBeatMessage.Type.PONG
import io.openfuture.chain.network.property.NodeProperties
import io.openfuture.chain.network.server.TcpServer
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.net.InetSocketAddress
import java.security.SecureRandom
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit.SECONDS

@Service
class DefaultNetworkInnerService(
    private val properties: NodeProperties,
    private val clock: NodeClock,
    private val bootstrap: Bootstrap,
    private val tcpServer: TcpServer,
    private val syncService: DefaultSyncBlockHandler
) : ApplicationListener<ApplicationReadyEvent>, InnerNetworkService {

    private val connections: MutableMap<Channel, NetworkAddressMessage> = ConcurrentHashMap()
    private val heartBeatTasks: MutableMap<Channel, ScheduledFuture<*>> = ConcurrentHashMap()

    companion object {
        private const val HEART_BEAT_DELAY = 0L
        private const val HEART_BEAT_INTERVAL = 20L
        private val log = LoggerFactory.getLogger(DefaultNetworkInnerService::class.java)
    }

    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        Executors.newSingleThreadExecutor().execute(tcpServer)
        maintainConnectionNumber()
    }

    @Scheduled(cron = "*/30 * * * * *")
    override fun maintainConnectionNumber() {
        if (isConnectionNeeded()) {
            requestAddresses()
        }
    }

    override fun getChannels(): Set<Channel> = connections.keys

    override fun onChannelActive(ctx: ChannelHandlerContext) {
        ctx.writeAndFlush(GreetingMessage(NetworkAddressMessage(properties.host!!, properties.port!!)))
    }

    override fun onClientChannelActive(ctx: ChannelHandlerContext) {
        val message = AskTimeMessage(clock.nodeTime())
        ctx.channel().writeAndFlush(message)

        val task = ctx.channel()
            .eventLoop()
            .scheduleAtFixedRate({ ctx.writeAndFlush(HeartBeatMessage(PING)) },
                HEART_BEAT_DELAY,
                HEART_BEAT_INTERVAL,
                SECONDS)
        heartBeatTasks[ctx.channel()] = task
    }

    override fun onHeartBeat(ctx: ChannelHandlerContext, heartBeat: HeartBeatMessage) {
        if (heartBeat.type == PING) {
            ctx.channel().writeAndFlush(HeartBeatMessage(PONG))
        }
    }

    override fun onFindAddresses(ctx: ChannelHandlerContext, message: FindAddressesMessage) {
        ctx.writeAndFlush(AddressesMessage(getConnectionAddresses().toList()))
    }


    override fun onAddresses(ctx: ChannelHandlerContext, message: AddressesMessage) {
        val peers = message.values
        val connections = getConnectionAddresses()
        peers.filter { !connections.contains(it) && it != NetworkAddressMessage(properties.host!!, properties.port!!) }
            .forEach { bootstrap.connect(it.host, it.port) }

        syncService.sync()
    }

    override fun onGreeting(ctx: ChannelHandlerContext, message: GreetingMessage) {
        connections[ctx.channel()] = message.address
    }

    override fun onAskTime(ctx: ChannelHandlerContext, askTime: AskTimeMessage) {
        ctx.channel().writeAndFlush(TimeMessage(askTime.nodeTimestamp, clock.networkTime()))
    }

    override fun onTime(ctx: ChannelHandlerContext, message: TimeMessage) {
        val offset = clock.calculateTimeOffset(message.nodeTimestamp, message.networkTimestamp)
        clock.addTimeOffset(ctx.channel().remoteAddress().toString(), offset)
    }

    override fun onChannelInactive(ctx: ChannelHandlerContext) {
        clock.removeTimeOffset(ctx.channel().remoteAddress().toString())
    }

    override fun onClientChannelInactive(ctx: ChannelHandlerContext) {
        connections.remove(ctx.channel())
        heartBeatTasks.remove(ctx.channel())!!.cancel(true)
    }

    private fun getConnectionAddresses(): Set<NetworkAddressMessage> = connections.values.toSet()

    private fun isConnectionNeeded(): Boolean = properties.peersNumber!! > getInboundConnections().size

    private fun getInboundConnections(): Map<Channel, NetworkAddressMessage> {
        return connections.filter {
            val socketAddress = it.key.remoteAddress() as InetSocketAddress
            NetworkAddressMessage(socketAddress.hostName, socketAddress.port) == it.value
        }
    }

    private fun requestAddresses() {
        val address = getConnectionAddresses().shuffled(SecureRandom()).firstOrNull()
            ?: properties.getRootAddresses().shuffled().first()
        send(address, FindAddressesMessage())
    }

    private fun send(address: NetworkAddressMessage, message: BaseMessage) {
        val channel = connections.filter { it.value == address }.map { it.key }.firstOrNull()
            ?: bootstrap.connect(address.host, address.port).addListener { future ->
                future as ChannelFuture
                if (!future.isSuccess) {
                    log.warn("Can not connect to ${address.host}:${address.port}")
                }
            }.channel()
        channel.writeAndFlush(message)
    }

}
