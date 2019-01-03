package io.openfuture.chain.network.handler.sync

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

import io.openfuture.chain.core.sync.SyncManager
import io.openfuture.chain.network.message.sync.EpochResponseMessage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.InetSocketAddress

@Component
@Sharable
class EpochResponseHandler(
    private val syncManager: SyncManager
) : SimpleChannelInboundHandler<EpochResponseMessage>() {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(EpochResponseHandler::class.java)
    }


    override fun channelRead0(ctx: ChannelHandlerContext, msg: EpochResponseMessage) {
        val inetAddress = (ctx.channel().remoteAddress() as InetSocketAddress).address
        log.debug("Get EpochResponseMessage from ${inetAddress.hostName}")
        syncManager.epochResponse(inetAddress, msg)
    }

}