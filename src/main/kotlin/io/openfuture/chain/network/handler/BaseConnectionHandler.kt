package io.openfuture.chain.network.handler

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.core.sync.SyncBlockHandler
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
    private val lock: ReentrantReadWriteLock,
    private var coreService: CoreMessageService,
    private val syncBlockHandler: SyncBlockHandler,
    protected var networkService: InnerNetworkService,
    private var consensusService: ConsensusMessageService
) : SimpleChannelInboundHandler<Packet>() {

    companion object {
        val log = LoggerFactory.getLogger(BaseConnectionHandler::class.java)!!
    }


    override fun channelActive(ctx: ChannelHandlerContext) {
        log.info("Connection with ${ctx.channel().remoteAddress()} established")
        networkService.onChannelActive(ctx)
    }

    override fun channelRead0(ctx: ChannelHandlerContext, packet: Packet) {
        when (packet.type) {
            HASH_BLOCK_REQUEST -> syncBlockHandler.blockHashRequest(ctx, packet.data as HashBlockRequestMessage)
            HASH_BLOCK_RESPONSE -> syncBlockHandler.blockHashResponse(ctx, packet.data as HashBlockResponseMessage)
            SYNC_BLOCKS_REQUEST -> syncBlockHandler.getBlocks(ctx, packet.data as SyncBlockRequestMessage)
            MAIN_BLOCK -> syncBlockHandler.saveBlocks(packet.data as MainBlockMessage)
            GENESIS_BLOCK -> syncBlockHandler.saveBlocks(packet.data as GenesisBlockMessage)
        }

        if(packet.type is HashMessage || packet.type is BlockMessage) {
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