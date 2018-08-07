package io.openfuture.chain.network.service

import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandlerContext
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
import java.lang.Math.max
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
    private val tcpServer: TcpServer
    ) : ApplicationListener<ApplicationReadyEvent>, InnerNetworkService {

    private val connections: MutableMap<Channel, NetworkAddressMessage> = ConcurrentHashMap()
    private val heartBeatTasks: MutableMap<Channel, ScheduledFuture<*>> = ConcurrentHashMap()
    private val knownAddresses: MutableSet<NetworkAddressMessage> = ConcurrentHashMap.newKeySet()

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
        log.info("Connected addresses ${getConnectionAddresses().size} : ${getConnectionAddresses()}")
        if (connectionNeededNumber() > 0) {
            requestAddresses()
        }
    }

    @Scheduled(cron = "0 */2 * * * *")
    override fun startExploring() {
        log.info("Known addresses ${knownAddresses.size} : $knownAddresses")
        knownAddresses.clear()
        val connectedAddresses = getConnectionAddresses()
        knownAddresses.addAll(connectedAddresses)
        connectedAddresses.forEach { send(it, ExplorerFindAddressesMessage()) }
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
        peers
            .filter { !connections.contains(it) && it != NetworkAddressMessage(properties.host!!, properties.port!!) }
            .shuffled()
            .take(connectionNeededNumber())
            .forEach { bootstrap.connect(it.host, it.port) }
    }

    override fun onExplorerFindAddresses(ctx: ChannelHandlerContext, message: ExplorerFindAddressesMessage) {
        ctx.writeAndFlush(ExplorerAddressesMessage(getConnectionAddresses().toList()))
    }

    override fun onExplorerAddresses(ctx: ChannelHandlerContext, message: ExplorerAddressesMessage) {
        message.values.forEach {
            if (!knownAddresses.contains(it)) {
                knownAddresses.add(it)
                sendAndClose(it, ExplorerFindAddressesMessage())
            }
        }
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

    private fun connectionNeededNumber(): Int = max(properties.peersNumber!! - getInboundConnections().size, 0)

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

    private fun sendAndClose(address: NetworkAddressMessage, message: BaseMessage) {
        val channel = connections.filter { it.value == address }.map { it.key }.firstOrNull()
            ?: bootstrap.connect(address.host, address.port).addListener { future ->
                future as ChannelFuture
                if (!future.isSuccess) {
                    log.warn("Can not connect to ${address.host}:${address.port}")
                }
            }.channel()
        channel.writeAndFlush(message)
        channel.eventLoop().schedule({ channel.close() }, 1, SECONDS)
    }

}
