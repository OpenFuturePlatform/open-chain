package io.openfuture.chain.network.service

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.network.message.TimeMessage
import io.openfuture.chain.network.message.application.block.BlockRequestMessage
import io.openfuture.chain.network.message.application.block.GenesisBlockMessage
import io.openfuture.chain.network.message.application.block.MainBlockMessage
import io.openfuture.chain.network.message.base.BaseMessage
import io.openfuture.chain.network.message.network.GreetingMessage
import io.openfuture.chain.network.message.network.HeartBeatMessage
import io.openfuture.chain.network.message.network.address.AddressesMessage
import io.openfuture.chain.network.message.network.address.FindAddressesMessage
import io.openfuture.chain.network.message.network.address.NetworkAddressMessage
import io.openfuture.chain.network.message.network.time.AskTimeMessage


interface NetworkService {

    fun broadcast(message: BaseMessage)

}

interface ConnectionService {

    fun maintainConnectionNumber()

    fun connect(peers: List<NetworkAddressMessage>)

    fun addConnection(channel: Channel, networkAddress: NetworkAddressMessage)

    fun removeConnection(channel: Channel): NetworkAddressMessage?

    fun getConnectionAddresses(): Set<NetworkAddressMessage>

    fun getConnections(): Map<Channel, NetworkAddressMessage>

}

interface ApplicationMessageService {

    fun onNetworkBlockRequest(ctx: ChannelHandlerContext, request: BlockRequestMessage)

    fun onGenesisBlock(ctx: ChannelHandlerContext, block: GenesisBlockMessage)

    fun onMainBlock(ctx: ChannelHandlerContext, block: MainBlockMessage)

}

interface NetworkMessageService {

    fun onChannelActive(ctx: ChannelHandlerContext)

    fun onClientChannelActive(ctx: ChannelHandlerContext)

    fun onHeartBeat(ctx: ChannelHandlerContext, heartBeat: HeartBeatMessage)

    fun onFindAddresses(ctx: ChannelHandlerContext, message: FindAddressesMessage)

    fun onAddresses(ctx: ChannelHandlerContext, message: AddressesMessage)

    fun onGreeting(ctx: ChannelHandlerContext, message: GreetingMessage)

    fun onAskTime(ctx: ChannelHandlerContext, askTime: AskTimeMessage)

    fun onTime(ctx: ChannelHandlerContext, message: TimeMessage)

    fun onChannelInactive(ctx: ChannelHandlerContext)

    fun onClientChannelInactive(ctx: ChannelHandlerContext)

}