package io.openfuture.chain.network.service

import io.netty.channel.Channel
import io.openfuture.chain.network.domain.NetworkAddress
import io.openfuture.chain.network.domain.Packet


interface NetworkService {

    fun broadcast(packet: Packet)

    fun maintainConnectionNumber()

    fun connect(peers: List<NetworkAddress>)

}

interface ConnectionService {

    fun addConnection(channel: Channel, networkAddress: NetworkAddress)

    fun removeConnection(channel: Channel): NetworkAddress?

    fun getConnectionAddresses(): Set<NetworkAddress>

    fun getConnections(): MutableMap<Channel, NetworkAddress>

}