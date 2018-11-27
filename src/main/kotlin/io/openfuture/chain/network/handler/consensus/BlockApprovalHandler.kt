package io.openfuture.chain.network.handler.consensus

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.consensus.component.block.PendingBlockHandler
import io.openfuture.chain.core.exception.SynchronizationException
import io.openfuture.chain.core.sync.SyncManager
import io.openfuture.chain.core.sync.SyncStatus
import io.openfuture.chain.network.component.ChannelsHolder
import io.openfuture.chain.network.message.consensus.BlockApprovalMessage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.dao.PessimisticLockingFailureException
import org.springframework.stereotype.Component

@Component
@Sharable
class BlockApprovalHandler(
    private val pendingBlockHandler: PendingBlockHandler,
    private val channelsHolder: ChannelsHolder,
    private val syncManager: SyncManager
) : SimpleChannelInboundHandler<BlockApprovalMessage>() {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(BlockApprovalHandler::class.java)
    }

    override fun channelRead0(ctx: ChannelHandlerContext, msg: BlockApprovalMessage) {
        if (syncManager.getStatus() != SyncStatus.SYNCHRONIZED) {
            log.debug("Block approval message decline")
            return
        }
        pendingBlockHandler.handleApproveMessage(msg)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        if (cause is PessimisticLockingFailureException) {
            log.error("Connection error ${ctx.channel().remoteAddress()} with cause: ${cause.message}")
        } else if (cause !is SynchronizationException) {
            log.error("Connection error ${ctx.channel().remoteAddress()} with cause: ${cause.message}")

            channelsHolder.removeChannel(ctx.channel())
        }
    }

}