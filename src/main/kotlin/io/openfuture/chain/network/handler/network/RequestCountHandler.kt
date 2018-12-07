package io.openfuture.chain.network.handler.network

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicLong

@Component
@Sharable
class RequestCountHandler : ChannelInboundHandlerAdapter() {
    private val fullCount: AtomicLong = AtomicLong(0)

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        fullCount.incrementAndGet()
        println("________________________")
        println(fullCount)
        println("________________________")
        ctx.fireChannelRead(msg)
    }

}