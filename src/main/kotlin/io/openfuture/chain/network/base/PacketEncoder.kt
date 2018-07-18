package io.openfuture.chain.network.base

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageEncoder
import io.openfuture.chain.network.domain.Packet
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class PacketEncoder : MessageToMessageEncoder<Packet>(){

    override fun encode(ctx: ChannelHandlerContext, msg: Packet, out: MutableList<Any>) {
        val buffer = ctx.alloc().buffer()
        Packet.write(msg, buffer)
        out.add(buffer)
    }
}