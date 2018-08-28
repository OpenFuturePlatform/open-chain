package io.openfuture.chain.network.handler

import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageEncoder
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.network.component.node.NodeClock
import io.openfuture.chain.network.message.base.BaseMessage
import io.openfuture.chain.network.message.base.Packet
import io.openfuture.chain.network.property.NodeProperties
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

    override fun encode(ctx: ChannelHandlerContext, data: BaseMessage, out: MutableList<Any>) {
        val packet = Packet(keyHolder.getUid(), data, properties.version!!, clock.networkTime())

        val buffer = ctx.alloc().buffer()
        packet.write(buffer)
        out.add(buffer)
    }

}