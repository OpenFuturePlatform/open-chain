package io.openfuture.chain.network.handler.base

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageEncoder
import io.openfuture.chain.network.component.node.NodeClock
import io.openfuture.chain.network.domain.base.BaseMessage
import io.openfuture.chain.network.domain.network.Packet
import io.openfuture.chain.property.NodeProperties
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(SCOPE_PROTOTYPE)
class PacketEncoder(
    private val properties: NodeProperties,
    private val clock: NodeClock
) : MessageToMessageEncoder<BaseMessage>() {

    companion object {
        private val log = LoggerFactory.getLogger(PacketEncoder::class.java)
    }


    override fun encode(ctx: ChannelHandlerContext, message: BaseMessage, out: MutableList<Any>) {
        val packet = Packet(message, properties.version!!, clock.networkTime())

        log.info("Encoding $message to ${ctx.channel().remoteAddress()}")

        val buffer = ctx.alloc().buffer()
        packet.write(buffer)
        out.add(buffer)
    }

}