package io.openfuture.chain.network.service

import io.netty.channel.Channel
import io.openfuture.chain.network.domain.NetworkAddress
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class DefaultConnectionService : ConnectionService {

    private val connections: MutableMap<Channel, NetworkAddress> = ConcurrentHashMap()

    override fun addConnection(channel: Channel, networkAddress: NetworkAddress) {
        connections[channel] = networkAddress
    }

    override fun removeConnection(channel: Channel): NetworkAddress? = connections.remove(channel)

    override fun getConnectionAddresses(): Set<NetworkAddress> = connections.values.toSet()

    override fun getConnections(): MutableMap<Channel, NetworkAddress> = connections

}