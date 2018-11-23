package io.openfuture.chain.network.service

import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.openfuture.chain.network.entity.NetworkAddress
import io.openfuture.chain.network.entity.NodeInfo
import io.openfuture.chain.network.serialization.Serializable
import org.springframework.scheduling.annotation.Async
import java.util.function.Consumer


interface NetworkApiService {

    fun broadcast(message: Serializable)

    fun isChannelsEmpty(): Boolean

    fun getConnectionSize(): Int

    fun sendRandom(message: Serializable)

    fun sendToAddress(message: Serializable, nodeInfo: NodeInfo)

    fun getNetworkSize(): Int

}

interface ConnectionService {

    fun connect(networkAddress: NetworkAddress, onConnect: Consumer<Channel>? = null): Boolean

    fun sendTimeSyncRequest()

    fun findNewPeer()

}