package io.openfuture.chain.network.handler.network.codec

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.message.base.Message
import io.openfuture.chain.network.message.base.MessageType
import io.openfuture.chain.network.property.NodeProperties
import org.springframework.stereotype.Component

@Component
@Sharable
class MessageEncoder(
    private val nodeProperties: NodeProperties
) : MessageToByteEncoder<Message>() {


    override fun encode(ctx: ChannelHandlerContext, message: Message, buf: ByteBuf) {
        buf.writeString(nodeProperties.protocolVersion!!)
        buf.writeLong(System.currentTimeMillis())
        buf.writeByte(MessageType.get(message).id.toInt())
        message.write(buf)
    }

}