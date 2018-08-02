package io.openfuture.chain.network.service

import io.netty.channel.Channel
import io.openfuture.chain.network.message.network.address.NetworkAddressMessage
import org.springframework.stereotype.Service
import java.net.InetSocketAddress
import java.util.concurrent.ConcurrentHashMap

@Service
class DefaultConnectionService : ConnectionService {

    private val connections: MutableMap<Channel, NetworkAddressMessage> = ConcurrentHashMap()

    override fun addConnection(channel: Channel, networkAddress: NetworkAddressMessage) {
        connections[channel] = networkAddress
    }

    override fun removeConnection(channel: Channel): NetworkAddressMessage? = connections.remove(channel)

    override fun getConnectionAddresses(): Set<NetworkAddressMessage> = connections.values.toSet()

    override fun getConnections(): Map<Channel, NetworkAddressMessage> = connections

    override fun getInboundConnections(): Map<Channel, NetworkAddressMessage> {
        return connections.filter {
            val socketAddress = it.key.remoteAddress() as InetSocketAddress
            NetworkAddressMessage(socketAddress.hostName, socketAddress.port) == it.value
        }
    }

}