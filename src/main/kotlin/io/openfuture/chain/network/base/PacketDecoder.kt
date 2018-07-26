package io.openfuture.chain.network.base

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ReplayingDecoder
import io.openfuture.chain.network.domain.Packet
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(SCOPE_PROTOTYPE)
class PacketDecoder(
    private val serializer: Packet.Serializer
) : ReplayingDecoder<ByteBuf>() {

    companion object {
        private val log = LoggerFactory.getLogger(PacketDecoder::class.java)
    }


    override fun decode(ctx: ChannelHandlerContext, bytes: ByteBuf, out: MutableList<Any>) {
        val packet = serializer.read(bytes)
        log.info("Decoded $packet from ${ctx.channel().remoteAddress()}")
        out.add(packet)
    }

}