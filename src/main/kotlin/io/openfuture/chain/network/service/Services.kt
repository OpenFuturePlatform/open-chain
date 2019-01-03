package io.openfuture.chain.network.service

import io.netty.channel.Channel
import io.openfuture.chain.network.entity.NetworkAddress
import io.openfuture.chain.network.entity.NodeInfo
import io.openfuture.chain.network.serialization.Serializable
import java.util.function.Consumer


interface NetworkApiService {

    fun broadcast(message: Serializable)

    fun isChannelsEmpty(): Boolean

    fun getConnectionSize(): Int

    fun sendToAddress(message: Serializable, nodeInfo: NodeInfo): Boolean

    fun getNetworkSize(): Int

    fun poll(message: Serializable, pollSize: Int)

}

interface ConnectionService {

    fun connect(networkAddress: NetworkAddress, onConnect: Consumer<Channel>? = null): Boolean

}