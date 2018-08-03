package io.openfuture.chain.network.service

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.network.message.base.BaseMessage
import io.openfuture.chain.network.message.consensus.BlockApprovalMessage
import io.openfuture.chain.network.message.consensus.PendingBlockMessage
import io.openfuture.chain.network.message.core.*
import io.openfuture.chain.network.message.network.*


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

interface CoreMessageService {

    fun onNetworkBlockRequest(ctx: ChannelHandlerContext, request: SyncBlockRequestMessage)

    fun onGenesisBlock(ctx: ChannelHandlerContext, block: GenesisBlockMessage)

    fun onMainBlock(ctx: ChannelHandlerContext, block: MainBlockMessage)

    fun onTransferTransaction(ctx: ChannelHandlerContext, tx: TransferTransactionMessage)

    fun onDelegateTransaction(ctx: ChannelHandlerContext, tx: DelegateTransactionMessage)

    fun onVoteTransaction(ctx: ChannelHandlerContext, tx: VoteTransactionMessage)

}

interface ConsensusMessageService {

    fun onBlockApproval(ctx: ChannelHandlerContext, block: BlockApprovalMessage)

    fun onPendingBlock(ctx: ChannelHandlerContext, block: PendingBlockMessage)

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