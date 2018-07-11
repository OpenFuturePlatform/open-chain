package io.openfuture.chain.network.base

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.util.ReferenceCountUtil
import io.openfuture.chain.protocol.CommunicationProtocol

abstract class BaseHandler(private val type: CommunicationProtocol.Type,
                           private val autoRelease: Boolean = true) : ChannelInboundHandlerAdapter() {

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        var release = true
        try {
            msg as CommunicationProtocol.Packet

            if (msg.type == type) {
                packetReceived(ctx, msg)
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

    abstract fun packetReceived(ctx: ChannelHandlerContext, message: CommunicationProtocol.Packet)

}