package io.openfuture.chain.network.service

import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.exception.ValidationException
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
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS

@Service
class DefaultNetworkInnerService(
    private val keyHolder: NodeKeyHolder,
    private val properties: NodeProperties,
    private val clock: NodeClock,
    private val bootstrap: Bootstrap,
    private val tcpServer: TcpServer
) : ApplicationListener<ApplicationReadyEvent>, NetworkInnerService {

    private val connections: MutableMap<Channel, AddressMessage> = ConcurrentHashMap()
    private val heartBeatTasks: MutableMap<Channel, ScheduledFuture<*>> = ConcurrentHashMap()
    private val knownUids: MutableSet<String> = ConcurrentHashMap.newKeySet()

    @Volatile
    private var networkSize: Int = 1

    companion object {
        private const val HEART_BEAT_INTERVAL = 20L
        private const val WAIT_FOR_RESPONSE_TIME = 1000L
        private const val CHECK_CONNECTIONS_PERIOD = 15000L
        private val log = LoggerFactory.getLogger(DefaultNetworkInnerService::class.java)
    }


    override fun onApplicationEvent(event: ApplicationReadyEvent) {
        Executors.newSingleThreadExecutor().execute(tcpServer)
    }

    @Scheduled(fixedRate = CHECK_CONNECTIONS_PERIOD)
    override fun maintainConnectionNumber() {
        if (connectionNeededNumber() > 0) {
            requestAddresses()
        }
    }

    @Scheduled(fixedRateString = "\${node.explorer-interval}")
    override fun startExploring() {
        networkSize = knownUids.size
        knownUids.clear()
        knownUids.add(keyHolder.getUid())
        val connectedAddresses = getConnectionAddresses()
        knownUids.addAll(connectedAddresses.map { it.uid })
        connectedAddresses.forEach { send(it, ExplorerFindAddressesMessage()) }
    }

    override fun getNetworkSize() = networkSize

    override fun getChannels(): Set<Channel> = connections.keys

    override fun getAddressMessage(uid: String): AddressMessage = getConnectionAddresses().firstOrNull { it.uid == uid }
        ?: throw NotFoundException("Not found address with such uid: $uid")

    override fun onChannelActive(ctx: ChannelHandlerContext) {
        ctx.writeAndFlush(GreetingMessage(properties.port!!))
    }

    override fun onClientChannelActive(ctx: ChannelHandlerContext) {
        val message = AskTimeMessage(clock.nodeTime())
        ctx.channel().writeAndFlush(message)

        val task = ctx.channel()
            .eventLoop()
            .scheduleAtFixedRate({ ctx.writeAndFlush(HeartBeatMessage(PING)) },
                HEART_BEAT_INTERVAL,
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
        val addresses = message.values
        val connectedAddress = getConnectionAddresses()
        addresses
            .filter { address ->
                !connectedAddress.map { it.uid }.contains(address.uid) && address.uid != keyHolder.getUid()
            }
            .shuffled()
            .take(connectionNeededNumber())
            .forEach { bootstrap.connect(it.address.host, it.address.port) }
    }

    override fun onExplorerFindAddresses(ctx: ChannelHandlerContext, message: ExplorerFindAddressesMessage) {
        ctx.writeAndFlush(ExplorerAddressesMessage(getConnectionAddresses().toList()))
    }

    override fun onExplorerAddresses(ctx: ChannelHandlerContext, message: ExplorerAddressesMessage) {
        message.values
            .filter { !knownUids.contains(it.uid) }
            .forEach {
                knownUids.add(it.uid)
                send(it, ExplorerFindAddressesMessage(), true)
            }
    }

    override fun onGreeting(ctx: ChannelHandlerContext, message: GreetingMessage, nodeUid: String) {
        val socket = ctx.channel().remoteAddress() as InetSocketAddress
        connections[ctx.channel()] = AddressMessage(nodeUid, NetworkAddressMessage(socket.address.hostAddress, message.port))
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
        connections.remove(ctx.channel())
    }

    override fun onClientChannelInactive(ctx: ChannelHandlerContext) {
        heartBeatTasks.remove(ctx.channel())!!.cancel(true)
    }

    override fun sendToAddress(message: BaseMessage, addressMessage: AddressMessage) {
        send(addressMessage, message, true)
    }

    override fun sendToRootNode(message: BaseMessage) {
        val address = getRootNodeAddress()
        send(address, message, true)
    }

    private fun requestAddresses() {
        getConnectionAddresses().shuffled(SecureRandom()).firstOrNull()?.let {
            send(it, FindAddressesMessage())
            return
        }

        send(getRootNodeAddress(), FindAddressesMessage())
    }

    private fun getRootNodeAddress() = properties.getRootAddresses()
        .shuffled()
        .firstOrNull() ?: throw ValidationException("There are no available network addresses")

    private fun send(addressMessage: AddressMessage, message: BaseMessage, closeAfterSending: Boolean = false) {
        if (addressMessage.uid == keyHolder.getUid()) {
            return
        }

        val channel = connections.filter { it.value.uid == addressMessage.uid }.map { it.key }.firstOrNull()
        if (channel != null) {
            sendAndCloseIfNeeded(channel, message, closeAfterSending)
        } else {
            send(addressMessage.address, message, closeAfterSending)
        }
    }

    private fun send(networkAddressMessage: NetworkAddressMessage, message: BaseMessage, closeAfterSending: Boolean = false) {
        bootstrap.connect(networkAddressMessage.host, networkAddressMessage.port).addListener { future ->
            if (future.isSuccess) {
                sendAndCloseIfNeeded((future as ChannelFuture).channel(), message, closeAfterSending)
            } else {
                log.warn("Can not connect to ${networkAddressMessage.host}:${networkAddressMessage.port}")
            }
        }
    }

    private fun getConnectionAddresses(): Set<AddressMessage> = connections.values.toSet()

    private fun connectionNeededNumber(): Int = max(properties.peersNumber!! - getInboundConnectionCount(), 0)

    private fun getInboundConnectionCount(): Int {
        return connections.count {
            val socketAddress = it.key.remoteAddress() as InetSocketAddress
            NetworkAddressMessage(socketAddress.address.hostAddress, socketAddress.port) == it.value.address
        }
    }

    private fun sendAndCloseIfNeeded(channel: Channel, message: BaseMessage, close: Boolean) {
        channel.writeAndFlush(message)
        if (close) {
            channel.eventLoop().schedule({ channel.close() }, WAIT_FOR_RESPONSE_TIME, MILLISECONDS)
        }
    }

}
