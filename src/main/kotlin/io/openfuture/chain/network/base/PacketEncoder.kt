package io.openfuture.chain.network.base

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageEncoder
import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.network.domain.Packet
import io.openfuture.chain.property.NodeProperty
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(SCOPE_PROTOTYPE)
class PacketEncoder(
    private val serializer: Packet.Serializer,
    private val property: NodeProperty,
    private val clock: NodeClock
) : MessageToMessageEncoder<Packet>() {

    companion object {
        private val log = LoggerFactory.getLogger(PacketEncoder::class.java)
    }


    override fun encode(ctx: ChannelHandlerContext, msg: Packet, out: MutableList<Any>) {
        msg.timestamp = clock.networkTime()
        msg.version = property.version

        log.info("Encoding $msg to ${ctx.channel().remoteAddress()}")

        val buffer = ctx.alloc().buffer()
        serializer.write(msg, buffer)
        out.add(buffer)
    }

}