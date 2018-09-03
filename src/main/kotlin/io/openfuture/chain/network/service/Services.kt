package io.openfuture.chain.network.service

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.network.entity.NetworkAddress
import io.openfuture.chain.network.message.network.ExplorerFindNodesMessage
import io.openfuture.chain.network.message.network.ExplorerNodesMessage
import io.openfuture.chain.network.serialization.Serializable


interface NetworkApiService {

    fun broadcast(message: Serializable)

    fun isChannelsEmpty(): Boolean

    fun getConnectionSize(): Int

    fun send(message: Serializable)

    fun sendToAddress(message: Serializable, address: NetworkAddress)

    fun getNetworkSize(): Int

}

interface NetworkInnerService {

    fun maintainConnectionNumber()

    fun startExploring()

    fun getNetworkSize(): Int

    fun getChannels(): Set<Channel>

    fun onExplorerFindAddresses(ctx: ChannelHandlerContext, message: ExplorerFindNodesMessage)

    fun onExplorerAddresses(ctx: ChannelHandlerContext, message: ExplorerNodesMessage)

    fun sendToAddress(message: Serializable, addressMessage: NetworkAddress)

    fun sendToRootNode(message: Serializable)

}

interface ConnectionService {

    fun connect(networkAddress: NetworkAddress)

}