package io.openfuture.chain.network.service

import io.netty.channel.Channel
import io.openfuture.chain.network.entity.NetworkAddress
import io.openfuture.chain.network.entity.NodeInfo
import io.openfuture.chain.network.message.base.Message
import java.util.function.Consumer


interface NetworkApiService {

    fun broadcast(message: Message)

    fun isChannelsEmpty(): Boolean

    fun getConnectionSize(): Int

    fun sendToAddress(message: Message, nodeInfo: NodeInfo)

    fun getNetworkSize(): Int

    fun poll(message: Message, pollSize: Int)

}

interface ConnectionService {

    fun connect(networkAddress: NetworkAddress, onConnect: Consumer<Channel>? = null): Boolean

}