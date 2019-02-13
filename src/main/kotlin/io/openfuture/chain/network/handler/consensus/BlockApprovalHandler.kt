package io.openfuture.chain.network.handler.consensus

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.consensus.component.block.PendingBlockHandler
import io.openfuture.chain.core.exception.CoreException
import io.openfuture.chain.core.exception.SynchronizationException
import io.openfuture.chain.network.message.consensus.BlockApprovalMessage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.dao.PessimisticLockingFailureException
import org.springframework.stereotype.Component

@Component
@Sharable
class BlockApprovalHandler(
    private val pendingBlockHandler: PendingBlockHandler
) : SimpleChannelInboundHandler<BlockApprovalMessage>() {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(BlockApprovalHandler::class.java)
    }


    override fun channelRead0(ctx: ChannelHandlerContext, msg: BlockApprovalMessage) {
        pendingBlockHandler.handleApproveMessage(msg)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        when (cause) {
            is SynchronizationException -> return
            is CoreException, is PessimisticLockingFailureException -> log.debug(cause.message)
            else -> {
                log.error("Connection error ${ctx.channel().remoteAddress()} with cause: ${cause.message}", cause)
                ctx.close()
            }
        }
    }

}