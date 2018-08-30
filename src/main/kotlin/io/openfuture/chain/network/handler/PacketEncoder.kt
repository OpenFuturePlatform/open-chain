package io.openfuture.chain.network.handler

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageEncoder
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.network.component.node.NodeClock
import io.openfuture.chain.network.message.base.BaseMessage
import io.openfuture.chain.network.message.base.Packet
import io.openfuture.chain.network.property.NodeProperties
import org.apache.commons.lang3.builder.ToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.BeanDefinition.SCOPE_PROTOTYPE
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(SCOPE_PROTOTYPE)
class PacketEncoder(
    private val keyHolder: NodeKeyHolder,
    private val properties: NodeProperties,
    private val clock: NodeClock
) : MessageToMessageEncoder<BaseMessage>() {

    companion object {
        private val log = LoggerFactory.getLogger(PacketEncoder::class.java)
    }


    override fun encode(ctx: ChannelHandlerContext, data: BaseMessage, out: MutableList<Any>) {
        val packet = Packet(keyHolder.getUid(), data, properties.version!!, clock.networkTime())

        log.debug("Encoding ${ToStringBuilder.reflectionToString(data, SHORT_PREFIX_STYLE)} " +
            "to ${ctx.channel().remoteAddress()}")

        val buffer = ctx.alloc().buffer()
        packet.write(buffer)
        out.add(buffer)
    }

}