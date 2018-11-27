package io.openfuture.chain.network.handler.sync

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.network.message.sync.SyncBlockRequestMessage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@Sharable
class SyncBlockRequestHandler(
    private val blockService: BlockService
) : SimpleChannelInboundHandler<SyncBlockRequestMessage>() {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(SyncBlockRequestHandler::class.java)
    }


    override fun channelRead0(ctx: ChannelHandlerContext, msg: SyncBlockRequestMessage) {
        log.debug("###########Start search blocks################# ")
        val blocksToSend = blockService.getAfterCurrentHash(msg.hash)
        log.debug(">>>>>Sending blocks size = ${blocksToSend.size} to ${ctx.channel().remoteAddress()}")
        blocksToSend.forEach { ctx.writeAndFlush(it.toMessage()) }
    }

}