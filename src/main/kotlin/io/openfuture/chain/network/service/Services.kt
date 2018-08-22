package io.openfuture.chain.network.service

import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.network.message.base.BaseMessage
import io.openfuture.chain.network.message.consensus.BlockApprovalMessage
import io.openfuture.chain.network.message.consensus.PendingBlockMessage
import io.openfuture.chain.network.message.core.DelegateTransactionMessage
import io.openfuture.chain.network.message.core.TransferTransactionMessage
import io.openfuture.chain.network.message.core.VoteTransactionMessage
import io.openfuture.chain.network.message.network.*


interface NetworkApiService {

    fun broadcast(message: BaseMessage)

    fun send(message: BaseMessage)

    fun sendToAddress(message: BaseMessage, addressMessage: AddressMessage)

    fun sendToRootNode(message: BaseMessage)

    fun getNetworkSize(): Int

    fun isChannelsEmpty(): Boolean

}

interface NetworkInnerService {

    fun maintainConnectionNumber()

    fun startExploring()

    fun getNetworkSize(): Int

    fun getChannels(): Set<Channel>

    fun getAddressMessage(uid: String): AddressMessage

    fun onChannelActive(ctx: ChannelHandlerContext)

    fun onClientChannelActive(ctx: ChannelHandlerContext)

    fun onHeartBeat(ctx: ChannelHandlerContext, heartBeat: HeartBeatMessage)

    fun onFindAddresses(ctx: ChannelHandlerContext, message: FindAddressesMessage)

    fun onAddresses(ctx: ChannelHandlerContext, message: AddressesMessage)

    fun onExplorerFindAddresses(ctx: ChannelHandlerContext, message: ExplorerFindAddressesMessage)

    fun onExplorerAddresses(ctx: ChannelHandlerContext, message: ExplorerAddressesMessage)

    fun onGreeting(ctx: ChannelHandlerContext, nodeUid: String)

    fun onAskTime(ctx: ChannelHandlerContext, askTime: AskTimeMessage)

    fun onTime(ctx: ChannelHandlerContext, message: TimeMessage)

    fun onChannelInactive(ctx: ChannelHandlerContext)

    fun onClientChannelInactive(ctx: ChannelHandlerContext)

    fun sendToAddress(message: BaseMessage, addressMessage: AddressMessage)

    fun sendToRootNode(message: BaseMessage)

}

interface CoreMessageService {

    fun onTransferTransaction(ctx: ChannelHandlerContext, message: TransferTransactionMessage)

    fun onDelegateTransaction(ctx: ChannelHandlerContext, message: DelegateTransactionMessage)

    fun onVoteTransaction(ctx: ChannelHandlerContext, message: VoteTransactionMessage)

}

interface ConsensusMessageService {

    fun onBlockApproval(ctx: ChannelHandlerContext, block: BlockApprovalMessage)

    fun onPendingBlock(ctx: ChannelHandlerContext, block: PendingBlockMessage)

}