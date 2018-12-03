package io.openfuture.chain.network.handler.network

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.openfuture.chain.network.component.MessageCache
import org.springframework.stereotype.Component

@Component
@ChannelHandler.Sharable
class CacheHandler(
    private val messageCache: MessageCache
) : ChannelInboundHandlerAdapter() {

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val buf = msg as ByteBuf
        val length = buf.readableBytes()
        val bytes = ByteArray(length)
        buf.readBytes(bytes)
        buf.resetReaderIndex()
        if (null == messageCache.getAndSaveHash(bytes)) {
            ctx.fireChannelRead(buf)
        }
    }

}