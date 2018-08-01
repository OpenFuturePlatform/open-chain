package io.openfuture.chain.network.handler.server

import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.openfuture.chain.network.handler.base.BaseConnectionHandler
import io.openfuture.chain.network.service.message.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@ChannelHandler.Sharable
class ConnectionServerHandler(
    addressHandler: AddressDiscoveryMessageService,
    greetingHandler: GreetingMessageService,
    heartBeatHandler: HeartBeatMessageService,
    timeSyncHandler: TimeSyncMessageService,
    blockHandler: BlockMessageService
) : BaseConnectionHandler(greetingHandler, addressHandler, heartBeatHandler, timeSyncHandler, blockHandler) {

    companion object {
        private val log = LoggerFactory.getLogger(ConnectionServerHandler::class.java)
    }


    override fun channelActive(ctx: ChannelHandlerContext) {
        log.info("Connection with ${ctx.channel().remoteAddress()} established")

        super.channelActive(ctx)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        log.info("Connection with ${ctx.channel().remoteAddress()} closed")

        super.channelInactive(ctx)
    }

}