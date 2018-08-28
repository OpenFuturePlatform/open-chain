package io.openfuture.chain.network.handler

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ReplayingDecoder
import io.openfuture.chain.network.message.base.Packet
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(SCOPE_PROTOTYPE)
class PacketDecoder : ReplayingDecoder<ByteBuf>() {

    override fun decode(ctx: ChannelHandlerContext, bytes: ByteBuf, out: MutableList<Any>) {
        val packet = Packet::class.java.newInstance()
        packet.read(bytes)
        out.add(packet)
    }

}