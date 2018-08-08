package io.openfuture.chain.network.handler

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.network.message.base.Packet
import io.openfuture.chain.network.message.base.PacketType.*
import io.openfuture.chain.network.message.consensus.BlockApprovalMessage
import io.openfuture.chain.network.message.consensus.PendingBlockMessage
import io.openfuture.chain.network.message.core.*
import io.openfuture.chain.network.message.network.*
import io.openfuture.chain.network.service.ConsensusMessageService
import io.openfuture.chain.network.service.CoreMessageService
import io.openfuture.chain.network.service.InnerNetworkService
import org.slf4j.LoggerFactory
import java.util.concurrent.locks.ReentrantReadWriteLock

abstract class BaseConnectionHandler(
    protected var networkService: InnerNetworkService,
    private var coreService: CoreMessageService,
    private var consensusService: ConsensusMessageService,
    private val lock: ReentrantReadWriteLock
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
            SYNC_BLOCKS_REQUEST -> coreService.onNetworkBlockRequest(ctx, packet.data as SyncBlockRequestMessage)
            MAIN_BLOCK -> coreService.onMainBlock(ctx, packet.data as MainBlockMessage)
            GENESIS_BLOCK -> coreService.onGenesisBlock(ctx, packet.data as GenesisBlockMessage)
        }

        if(listOf(SYNC_BLOCKS_REQUEST, MAIN_BLOCK, GENESIS_BLOCK).contains(packet.type)) {
            return
        }

        lock.readLock().lock()
        try {
            when (packet.type) {
                HEART_BEAT -> networkService.onHeartBeat(ctx, packet.data as HeartBeatMessage)
                TRANSFER_TRANSACTION -> coreService.onTransferTransaction(ctx, packet.data as TransferTransactionMessage)
                DELEGATE_TRANSACTION -> coreService.onDelegateTransaction(ctx, packet.data as DelegateTransactionMessage)
                VOTE_TRANSACTION -> coreService.onVoteTransaction(ctx, packet.data as VoteTransactionMessage)
                BLOCK_APPROVAL -> consensusService.onBlockApproval(ctx, packet.data as BlockApprovalMessage)
                PENDING_BLOCK -> consensusService.onPendingBlock(ctx, packet.data as PendingBlockMessage)
                GREETING -> networkService.onGreeting(ctx, packet.data as GreetingMessage)
                ADDRESSES -> networkService.onAddresses(ctx, packet.data as AddressesMessage)
                FIND_ADDRESSES -> networkService.onFindAddresses(ctx, packet.data as FindAddressesMessage)
                TIME -> networkService.onTime(ctx, packet.data as TimeMessage)
                ASK_TIME -> networkService.onAskTime(ctx, packet.data as AskTimeMessage)
            }
        } finally {
            lock.readLock().unlock()
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