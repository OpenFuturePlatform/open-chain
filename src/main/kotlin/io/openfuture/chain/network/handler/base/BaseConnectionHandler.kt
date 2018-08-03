package io.openfuture.chain.network.handler.base

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.network.message.application.block.*
import io.openfuture.chain.network.message.application.transaction.DelegateTransactionMessage
import io.openfuture.chain.network.message.application.transaction.TransferTransactionMessage
import io.openfuture.chain.network.message.application.transaction.VoteTransactionMessage
import io.openfuture.chain.network.message.network.GreetingMessage
import io.openfuture.chain.network.message.network.HeartBeatMessage
import io.openfuture.chain.network.message.base.Packet
import io.openfuture.chain.network.message.base.PacketType.*
import io.openfuture.chain.network.message.network.address.AddressesMessage
import io.openfuture.chain.network.message.network.address.FindAddressesMessage
import io.openfuture.chain.network.message.network.time.AskTimeMessage
import io.openfuture.chain.network.message.network.time.TimeMessage
import io.openfuture.chain.network.service.DefaultApplicationMessageService
import io.openfuture.chain.network.service.NetworkMessageService
import org.slf4j.LoggerFactory

abstract class BaseConnectionHandler(
    protected val networkService: NetworkMessageService,
    protected val applicationService: DefaultApplicationMessageService
) : SimpleChannelInboundHandler<Packet>() {

    companion object {
        val log = LoggerFactory.getLogger(BaseConnectionHandler::class.java)
    }


    override fun channelActive(ctx: ChannelHandlerContext) {
        log.info("Connection with ${ctx.channel().remoteAddress()} established")
        networkService.onChannelActive(ctx)
    }

    override fun channelRead0(ctx: ChannelHandlerContext, packet: Packet) {
        when(packet.type) {
            ADDRESSES -> networkService.onAddresses(ctx, packet.data as AddressesMessage)
            FIND_ADDRESSES -> networkService.onFindAddresses(ctx, packet.data as FindAddressesMessage)
            GREETING -> networkService.onGreeting(ctx, packet.data as GreetingMessage)
            HEART_BEAT -> networkService.onHeartBeat(ctx, packet.data as HeartBeatMessage)
            TIME -> networkService.onTime(ctx, packet.data as TimeMessage)
            ASK_TIME -> networkService.onAskTime(ctx, packet.data as AskTimeMessage)
            MAIN_BLOCK -> applicationService.onMainBlock(ctx, packet.data as MainBlockMessage)
            GENESIS_BLOCK -> applicationService.onGenesisBlock(ctx, packet.data as GenesisBlockMessage)
            SYNC_BLOCKS_REQUEST -> applicationService.onNetworkBlockRequest(ctx, packet.data as SyncBlockRequestMessage)
            PENDING_BLOCK -> applicationService.onPendingBlock(ctx, packet.data as PendingBlockMessage)
            BLOCK_APPROVAL -> applicationService.onBlockApproval(ctx, packet.data as BlockApprovalMessage)
            TRANSFER_TRANSACTION -> applicationService.onTransferTransaction(ctx, packet.data as TransferTransactionMessage)
            DELEGATE_TRANSACTION -> applicationService.onDelegateTransaction(ctx, packet.data as DelegateTransactionMessage)
            VOTE_TRANSACTION -> applicationService.onVoteTransaction(ctx, packet.data as VoteTransactionMessage)
        }
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        log.info("Connection with ${ctx.channel().remoteAddress()} closed")
        networkService.onChannelInactive(ctx)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        log.error("Connection error ${ctx.channel().remoteAddress()} with $cause")
        ctx.close()
    }

}