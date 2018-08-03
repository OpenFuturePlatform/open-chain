package io.openfuture.chain.network.service

import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.openfuture.chain.network.message.base.BaseMessage
import io.openfuture.chain.network.message.network.FindAddressesMessage
import io.openfuture.chain.network.message.network.NetworkAddressMessage
import io.openfuture.chain.network.property.NodeProperties
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.net.InetSocketAddress
import java.security.SecureRandom
import java.util.concurrent.ConcurrentHashMap

@Service
class DefaultConnectionService(
    private val bootstrap: Bootstrap,
    private val properties: NodeProperties
) : ConnectionService {

    private val connections: MutableMap<Channel, NetworkAddressMessage> = ConcurrentHashMap()

    companion object {
        private val log = LoggerFactory.getLogger(DefaultNetworkService::class.java)

    }


    @Scheduled(cron = "*/30 * * * * *")
    override fun maintainConnectionNumber() {
        if (isConnectionNeeded()) {
            requestAddresses()
        }
    }

    override fun connect(peers: List<NetworkAddressMessage>) {
        val connections = getConnectionAddresses()
        peers.filter { !connections.contains(it) && it != NetworkAddressMessage(properties.host!!, properties.port!!) }
            .forEach { bootstrap.connect(it.host, it.port) }
    }

    override fun addConnection(channel: Channel, networkAddress: NetworkAddressMessage) {
        connections[channel] = networkAddress
    }

    override fun removeConnection(channel: Channel): NetworkAddressMessage? = connections.remove(channel)

    override fun getConnectionAddresses(): Set<NetworkAddressMessage> = connections.values.toSet()

    override fun getConnections(): Map<Channel, NetworkAddressMessage> = connections

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