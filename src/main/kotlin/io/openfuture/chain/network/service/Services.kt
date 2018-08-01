package io.openfuture.chain.network.service

import io.netty.channel.Channel
import io.openfuture.chain.network.domain.base.BaseMessage
import io.openfuture.chain.network.domain.network.address.NetworkAddressMessage


interface NetworkService {

    fun broadcast(message: BaseMessage)

    fun maintainConnectionNumber()

    fun connect(peers: List<NetworkAddressMessage>)

}

interface ConnectionService {

    fun addConnection(channel: Channel, networkAddress: NetworkAddressMessage)

    fun removeConnection(channel: Channel): NetworkAddressMessage?

    fun getConnectionAddresses(): Set<NetworkAddressMessage>

    fun getConnections(): Map<Channel, NetworkAddressMessage>

    fun getInboundConnections(): Map<Channel, NetworkAddressMessage>

}