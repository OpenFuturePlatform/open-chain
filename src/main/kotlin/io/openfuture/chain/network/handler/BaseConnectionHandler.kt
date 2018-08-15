package io.openfuture.chain.network.handler

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.network.message.base.Packet
import io.openfuture.chain.network.message.base.PacketType.*
import io.openfuture.chain.network.message.consensus.BlockApprovalMessage
import io.openfuture.chain.network.message.consensus.PendingBlockMessage
import io.openfuture.chain.network.message.core.MainBlockMessage
import io.openfuture.chain.network.message.core.*
import io.openfuture.chain.network.message.network.*
import io.openfuture.chain.network.service.ConsensusMessageService
import io.openfuture.chain.network.service.CoreMessageService
import io.openfuture.chain.network.service.NetworkInnerService
import io.openfuture.chain.network.sync.SyncBlockHandler
import io.openfuture.chain.network.sync.SyncManager
import io.openfuture.chain.network.sync.impl.SynchronizationStatus.SYNCHRONIZED
import org.slf4j.LoggerFactory

abstract class BaseConnectionHandler(
    private var coreService: CoreMessageService,
    private val syncManager: SyncManager,
    private val syncBlockHandler: SyncBlockHandler,
    protected var networkService: NetworkInnerService,
    private var consensusService: ConsensusMessageService
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
            HEART_BEAT -> networkService.onHeartBeat(ctx, packet.data as HeartBeatMessage)
            GREETING -> networkService.onGreeting(ctx, packet.data as GreetingMessage)
            ADDRESSES -> networkService.onAddresses(ctx, packet.data as AddressesMessage)
            FIND_ADDRESSES -> networkService.onFindAddresses(ctx, packet.data as FindAddressesMessage)
            TIME -> networkService.onTime(ctx, packet.data as TimeMessage)
            ASK_TIME -> networkService.onAskTime(ctx, packet.data as AskTimeMessage)
            EXPLORER_FIND_ADDRESSES -> networkService.onExplorerFindAddresses(ctx, packet.data as ExplorerFindAddressesMessage)
            EXPLORER_ADDRESSES -> networkService.onExplorerAddresses(ctx, packet.data as ExplorerAddressesMessage)

            DELEGATE_REQUEST -> coreService.onFindDelegates(ctx, packet.data as DelegateRequestMessage)
            DELEGATE_RESPONSE -> syncBlockHandler.onDelegateResponseMessage(ctx, packet.data as DelegateResponseMessage)
            HASH_BLOCK_REQUEST -> syncBlockHandler.onHashBlockRequestMessage(ctx, packet.data as HashBlockRequestMessage)
            HASH_BLOCK_RESPONSE -> syncBlockHandler.onHashResponseMessage(ctx, packet.data as HashBlockResponseMessage)
            SYNC_BLOCKS_REQUEST -> syncBlockHandler.onSyncBlocRequestMessage(ctx, packet.data as SyncBlockRequestMessage)
            MAIN_BLOCK -> syncBlockHandler.onMainBlockMessage(packet.data as MainBlockMessage)
            GENESIS_BLOCK -> syncBlockHandler.onGenesisBlockMessage(packet.data as GenesisBlockMessage)
        }

        if (syncManager.getSyncStatus() == SYNCHRONIZED) {
            processAppMessages(ctx, packet)
        }
    }

    private fun processAppMessages(ctx: ChannelHandlerContext, packet: Packet) {
        when (packet.type) {
            TRANSFER_TRANSACTION -> coreService.onTransferTransaction(ctx, packet.data as TransferTransactionMessage)
            DELEGATE_TRANSACTION -> coreService.onDelegateTransaction(ctx, packet.data as DelegateTransactionMessage)
            VOTE_TRANSACTION -> coreService.onVoteTransaction(ctx, packet.data as VoteTransactionMessage)
            BLOCK_APPROVAL -> consensusService.onBlockApproval(ctx, packet.data as BlockApprovalMessage)
            PENDING_BLOCK -> consensusService.onPendingBlock(ctx, packet.data as PendingBlockMessage)
        }
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        log.info("Connection with ${ctx.channel().remoteAddress()} closed")
        networkService.onChannelInactive(ctx)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        log.error("Connection error ${ctx.channel().remoteAddress()} with cause", cause)
        //ctx.channel().close() TODO: uncomment this once block chain synchronize logic will handle ValidationException
    }

}