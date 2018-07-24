package io.openfuture.chain.network.base

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.util.ReferenceCountUtil

abstract class BaseHandler<T>(private val autoRelease: Boolean = true) : ChannelInboundHandlerAdapter() {

    private val genericClass = this::class.supertypes[0].arguments[0].type!!.classifier

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        var release = true
        try {
            if (msg::class == genericClass || msg::class.supertypes[0].classifier == genericClass) {
                @Suppress("UNCHECKED_CAST")
                packetReceived(ctx, msg as T)
            } else {
                release = false
                ctx.fireChannelRead(msg)
            }
        } finally {
            if (autoRelease && release) {
                ReferenceCountUtil.release(msg)
            }
        }
    }

    abstract fun packetReceived(ctx: ChannelHandlerContext, message: T)

}