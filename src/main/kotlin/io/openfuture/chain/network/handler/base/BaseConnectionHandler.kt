package io.openfuture.chain.network.handler.base

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.network.message.core.DelegateTransactionMessage
import io.openfuture.chain.network.message.core.TransferTransactionMessage
import io.openfuture.chain.network.message.core.VoteTransactionMessage
import io.openfuture.chain.network.message.base.Packet
import io.openfuture.chain.network.message.base.PacketType.*
import io.openfuture.chain.network.message.consensus.BlockApprovalMessage
import io.openfuture.chain.network.message.consensus.PendingBlockMessage
import io.openfuture.chain.network.message.core.GenesisBlockMessage
import io.openfuture.chain.network.message.core.MainBlockMessage
import io.openfuture.chain.network.message.core.SyncBlockRequestMessage
import io.openfuture.chain.network.message.network.GreetingMessage
import io.openfuture.chain.network.message.network.HeartBeatMessage
import io.openfuture.chain.network.message.network.AddressesMessage
import io.openfuture.chain.network.message.network.FindAddressesMessage
import io.openfuture.chain.network.message.network.AskTimeMessage
import io.openfuture.chain.network.message.network.TimeMessage
import io.openfuture.chain.network.service.ConsensusMessageService
import io.openfuture.chain.network.service.CoreMessageService
import io.openfuture.chain.network.service.NetworkMessageService
import org.slf4j.LoggerFactory

abstract class BaseConnectionHandler(
    protected val networkService: NetworkMessageService,
    protected val coreService: CoreMessageService,
    protected val consensusService: ConsensusMessageService
) : SimpleChannelInboundHandler<Packet>() {

    companion object {
        val log = LoggerFactory.getLogger(BaseConnectionHandler::class.java)
    }


    override fun channelActive(ctx: ChannelHandlerContext) {
        log.info("Connection with ${ctx.channel().remoteAddress()} established")
        networkService.onChannelActive(ctx)
    }

    override fun channelRead0(ctx: ChannelHandlerContext, packet: Packet) {
        when (packet.type) {
            ADDRESSES -> networkService.onAddresses(ctx, packet.data as AddressesMessage)
            FIND_ADDRESSES -> networkService.onFindAddresses(ctx, packet.data as FindAddressesMessage)
            GREETING -> networkService.onGreeting(ctx, packet.data as GreetingMessage)
            HEART_BEAT -> networkService.onHeartBeat(ctx, packet.data as HeartBeatMessage)
            TIME -> networkService.onTime(ctx, packet.data as TimeMessage)
            ASK_TIME -> networkService.onAskTime(ctx, packet.data as AskTimeMessage)
            MAIN_BLOCK -> coreService.onMainBlock(ctx, packet.data as MainBlockMessage)
            GENESIS_BLOCK -> coreService.onGenesisBlock(ctx, packet.data as GenesisBlockMessage)
            SYNC_BLOCKS_REQUEST -> coreService.onNetworkBlockRequest(ctx, packet.data as SyncBlockRequestMessage)
            TRANSFER_TRANSACTION -> coreService.onTransferTransaction(ctx, packet.data as TransferTransactionMessage)
            DELEGATE_TRANSACTION -> coreService.onDelegateTransaction(ctx, packet.data as DelegateTransactionMessage)
            VOTE_TRANSACTION -> coreService.onVoteTransaction(ctx, packet.data as VoteTransactionMessage)
            PENDING_BLOCK -> consensusService.onPendingBlock(ctx, packet.data as PendingBlockMessage)
            BLOCK_APPROVAL -> consensusService.onBlockApproval(ctx, packet.data as BlockApprovalMessage)
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