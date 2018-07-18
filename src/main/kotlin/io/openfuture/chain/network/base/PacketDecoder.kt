package io.openfuture.chain.network.base

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ReplayingDecoder
import io.openfuture.chain.network.domain.Packet
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope("prototype")
class PacketDecoder : ReplayingDecoder<ByteBuf>() {

    override fun decode(ctx: ChannelHandlerContext, bytes: ByteBuf, out: MutableList<Any>) {
        out.add(Packet.read(bytes))
    }
}