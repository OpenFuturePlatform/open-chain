package io.openfuture.chain.network.handler.network.codec

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ReplayingDecoder
import io.openfuture.chain.network.component.time.Clock
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.message.base.MessageType
import io.openfuture.chain.network.message.network.RequestTimeMessage
import io.openfuture.chain.network.message.network.ResponseTimeMessage
import io.openfuture.chain.network.message.sync.GenesisBlockMessage
import io.openfuture.chain.network.message.sync.MainBlockMessage
import io.openfuture.chain.network.property.NodeProperties
import io.openfuture.chain.network.serialization.Serializable
import org.apache.commons.lang3.builder.ToStringBuilder
import org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(SCOPE_PROTOTYPE)
class MessageDecoder(
    private val nodeProperties: NodeProperties,
    private val clock: Clock
) : ReplayingDecoder<Nothing>() {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(MessageDecoder::class.java)
    }


    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        val messageVersion = buf.readString()
        val protocolVersion = nodeProperties.protocolVersion!!
        if (protocolVersion != messageVersion) {
            log.warn("Version discrepancy. Your version is: $protocolVersion, incoming version is: $messageVersion")
            return
        }

        val originTime = buf.readLong() //timestamp
        val type = MessageType.get(buf.readByte())
        val message = type.clazz.java.getConstructor().newInstance()

        message.read(buf)

        if (isExpired(message, originTime)) {
            log.debug("Message $type from ${ctx.channel().remoteAddress()} decline by expiration")
            return
        }

        log.trace("Decoded ${ToStringBuilder.reflectionToString(message, SHORT_PREFIX_STYLE)} " +
            "from ${ctx.channel().remoteAddress()}")

        out.add(message)
    }

    private fun isExpired(message: Serializable, originTime: Long): Boolean {
        if (message is RequestTimeMessage || message is ResponseTimeMessage || message is MainBlockMessage || message is GenesisBlockMessage) {
            return false
        }

        return (clock.currentTimeMillis() - originTime) > nodeProperties.expiry!!
    }

}