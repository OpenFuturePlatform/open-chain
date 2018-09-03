package io.openfuture.chain.network.service

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.network.entity.NetworkAddress
import io.openfuture.chain.network.message.network.ExplorerFindNodesMessage
import io.openfuture.chain.network.message.network.ExplorerNodesMessage
import io.openfuture.chain.network.serialization.Serializable
import org.springframework.stereotype.Service

//
//import io.netty.bootstrap.Bootstrap
//import io.netty.channel.Channel
//import io.netty.channel.ChannelFuture
//import io.netty.channel.ChannelHandlerContext
//import io.openfuture.chain.core.component.NodeConfigurator
//import io.openfuture.chain.core.component.NodeKeyHolder
//import io.openfuture.chain.core.exception.NotFoundException
//import io.openfuture.chain.core.exception.ValidationException
//import io.openfuture.chain.network.component.NodeClock
//import io.openfuture.chain.network.entity.NetworkAddress
//import io.openfuture.chain.network.message.network.*
//import io.openfuture.chain.network.message.network.HeartBeatMessage.Type.PING
//import io.openfuture.chain.network.message.network.HeartBeatMessage.Type.PONG
//import io.openfuture.chain.network.property.NodeProperties
//import io.openfuture.chain.network.serialization.Serializable
//import io.openfuture.chain.network.server.TcpServer
//import org.slf4j.LoggerFactory
//import org.springframework.boot.context.event.ApplicationReadyEvent
//import org.springframework.context.ApplicationListener
//import org.springframework.scheduling.annotation.Scheduled
//import org.springframework.stereotype.Service
//import java.lang.Math.max
//import java.net.InetSocketAddress
//import java.security.SecureRandom
//import java.util.concurrent.ConcurrentHashMap
//import java.util.concurrent.Executors
//import java.util.concurrent.ScheduledFuture
//import java.util.concurrent.TimeUnit.MILLISECONDS
//import java.util.concurrent.TimeUnit.SECONDS
//
@Service
class DefaultNetworkInnerService() : NetworkInnerService {
    override fun maintainConnectionNumber() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun startExploring() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getNetworkSize(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getChannels(): Set<Channel> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onExplorerFindAddresses(ctx: ChannelHandlerContext, message: ExplorerFindNodesMessage) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onExplorerAddresses(ctx: ChannelHandlerContext, message: ExplorerNodesMessage) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sendToAddress(message: Serializable, addressMessage: NetworkAddress) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sendToRootNode(message: Serializable) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
//@Service
//class DefaultNetworkInnerService(
//    private val keyHolder: NodeKeyHolder,
//    private val properties: NodeProperties,
//    private val config: NodeConfigurator,
//    private val clock: NodeClock,
//    private val bootstrap: Bootstrap
//) :  NetworkInnerService {
//
//    private val connections: MutableMap<Channel, AddressMessage> = ConcurrentHashMap()
//    private val heartBeatTasks: MutableMap<Channel, ScheduledFuture<*>> = ConcurrentHashMap()
//    private val knownUids: MutableSet<String> = ConcurrentHashMap.newKeySet()
//
//    @Volatile
//    private var networkSize: Int = 1
//
//    companion object {
//        private const val HEART_BEAT_INTERVAL = 20L
//        private const val WAIT_FOR_RESPONSE_TIME = 1000L
//        private const val CHECK_CONNECTIONS_PERIOD = 15000L
//        private val log = LoggerFactory.getLogger(DefaultNetworkInnerService::class.java)
//    }

//    @Scheduled(fixedRateString = "\${node.explorer-interval}")
//    override fun startExploring() {
//        networkSize = knownUids.size
//        knownUids.clear()
//        knownUids.add(keyHolder.getUid())
//        val connectedAddresses = getConnectionAddresses()
//        knownUids.addAll(connectedAddresses.map { it.uid })
//        connectedAddresses.forEach { send(it, ExplorerFindNodesMessage()) }
//    }
//
//    override fun getNetworkSize() = networkSize

//    override fun onExplorerFindAddresses(ctx: ChannelHandlerContext, message: ExplorerFindNodesMessage) {
//        ctx.writeAndFlush(ExplorerNodesMessage(getConnectionAddresses().toList()))
//    }
//
//    override fun onExplorerAddresses(ctx: ChannelHandlerContext, message: ExplorerNodesMessage) {
//        message.values
//            .filter { !knownUids.contains(it.uid) }
//            .forEach {
//                knownUids.add(it.uid)
//                send(it, ExplorerFindNodesMessage(), true)
//            }
//    }
//
//    private fun send(addressMessage: NetworkAddress, message: Serializable, closeAfterSending: Boolean = false) {
//        if (addressMessage.uid == keyHolder.getUid()) {
//            return
//        }
//
//        send(addressMessage.address, message, closeAfterSending)
//    }
//
//    private fun send(networkAddressMessage: NetworkAddress, message: Serializable, closeAfterSending: Boolean = false) {
//        val channel = connections.filter { it.value.address == networkAddressMessage }.map { it.key }.firstOrNull()
//        if (channel != null) {
//            sendAndCloseIfNeeded(channel, message, closeAfterSending)
//        } else {
//            bootstrap.connect(networkAddressMessage.host, networkAddressMessage.port).addListener { future ->
//                if (future.isSuccess) {
//                    sendAndCloseIfNeeded((future as ChannelFuture).channel(), message, closeAfterSending)
//                } else {
//                    log.warn("Can not connect to ${networkAddressMessage.host}:${networkAddressMessage.port}")
//                }
//            }
//        }
//    }
//
//
//    private fun sendAndCloseIfNeeded(channel: Channel, message: Serializable, close: Boolean) {
//        channel.writeAndFlush(message)
//        if (close) {
//            channel.eventLoop().schedule({ channel.close() }, WAIT_FOR_RESPONSE_TIME, MILLISECONDS)
//        }
//    }
//
//}
