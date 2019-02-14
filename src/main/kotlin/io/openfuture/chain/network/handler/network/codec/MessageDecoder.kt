package io.openfuture.chain.network.handler.network.codec

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ReplayingDecoder
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.message.base.Message
import io.openfuture.chain.network.message.base.MessageType
import io.openfuture.chain.network.message.sync.GenesisBlockMessage
import io.openfuture.chain.network.message.sync.MainBlockMessage
import io.openfuture.chain.network.property.NodeProperties
import org.slf4j.Logger
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
        private val log: Logger = LoggerFactory.getLogger(MessageDecoder::class.java)
    }


    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        val originTime = readTimeAndCheckVersion(buf) ?: return

        val type = MessageType.get(buf.readByte())
        val message = type.clazz.java.getConstructor().newInstance()

        message.read(buf)
        if (isExpired(message, originTime)) {
            return
        }

        out.add(message)
    }

    fun readTimeAndCheckVersion(buf: ByteBuf): Long? {
        val messageVersion = buf.readString()
        val protocolVersion = nodeProperties.protocolVersion!!
        if (protocolVersion != messageVersion) {
            log.warn("Version discrepancy. Your version is: $protocolVersion, incoming version is: $messageVersion")
            return null
        }

        return buf.readLong()
    }

    private fun isExpired(message: Message, originTime: Long): Boolean {
        if (message is MainBlockMessage || message is GenesisBlockMessage) {
            return false
        }

        return (System.currentTimeMillis() - originTime) > nodeProperties.expiry!!
    }

}