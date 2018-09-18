package io.openfuture.chain.network.handler.network.codec

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ReplayingDecoder
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.message.base.MessageType
import io.openfuture.chain.network.property.NodeProperties
import org.apache.commons.lang3.builder.ToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(SCOPE_PROTOTYPE)
class MessageDecoder(
    private val nodeProperties: NodeProperties
) : ReplayingDecoder<Nothing>() {

    companion object {
        private val log = LoggerFactory.getLogger(MessageDecoder::class.java)
    }


    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        val messageVersion = buf.readString()
        val nodeVersion = nodeProperties.version!!
        if (nodeVersion != messageVersion) {
            log.warn("Version discrepancy. Your version is: $nodeVersion, incoming version is: $messageVersion")
            return
        }

        buf.readLong() //timestamp
        val type = MessageType.get(buf.readByte())
        val message = type.clazz.java.newInstance()
        message.read(buf)

        log.debug("Decoded ${ToStringBuilder.reflectionToString(message, SHORT_PREFIX_STYLE)} " +
            "from ${ctx.channel().remoteAddress()}")

        out.add(message)
    }

}