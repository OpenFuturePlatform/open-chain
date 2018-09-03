package io.openfuture.chain.network.handler.network.codec

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.CombinedChannelDuplexHandler
import io.openfuture.chain.network.component.ChannelsHolder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(SCOPE_PROTOTYPE)
class MessageCodec(
    private val channelsHolder: ChannelsHolder,
    decoder: MessageDecoder,
    encoder: MessageEncoder
) : CombinedChannelDuplexHandler<MessageDecoder, MessageEncoder>(decoder, encoder) {

    companion object {
        private val log = LoggerFactory.getLogger(MessageCodec::class.java)
    }


    override fun channelActive(ctx: ChannelHandlerContext) {
        log.debug("Connection with ${ctx.channel().remoteAddress()} established")
        super.channelActive(ctx)
    }

    override fun channelInactive(ctx: ChannelHandlerContext) {
        log.debug("Connection with ${ctx.channel().remoteAddress()} closed")
        channelsHolder.removeChannel(ctx.channel())
        super.channelInactive(ctx)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        log.error("Connection error ${ctx.channel().remoteAddress()} with cause", cause)
        channelsHolder.removeChannel(ctx.channel())
        ctx.close()
    }

}