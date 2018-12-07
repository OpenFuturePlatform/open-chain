package io.openfuture.chain.network.handler.network

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.openfuture.chain.network.component.MessageCache
import io.openfuture.chain.network.handler.network.codec.MessageDecoder
import org.springframework.stereotype.Component

@Component
@ChannelHandler.Sharable
class CacheHandler(
    private val messageCache: MessageCache,
    private val messageDecoder: MessageDecoder
) : ChannelInboundHandlerAdapter() {

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        val buf = msg as ByteBuf
        messageDecoder.readTimeAndCheckVersion(buf)
        val length = buf.readableBytes() - buf.readerIndex()
        val bytes = ByteArray(length)
        buf.readBytes(bytes)
        buf.resetReaderIndex()
        if (!messageCache.saveIfAbsent(bytes)) {
            ctx.fireChannelRead(buf)
        }
    }

}