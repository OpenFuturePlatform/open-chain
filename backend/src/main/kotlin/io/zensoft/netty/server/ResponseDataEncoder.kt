package io.zensoft.netty.server

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import java.nio.charset.Charset


class ResponseDataEncoder : MessageToByteEncoder<String>() {

    private val charset = Charset.forName("UTF-8")

    @Throws(Exception::class)
    override fun encode(ctx: ChannelHandlerContext, msg: String, out: ByteBuf) {
        out.writeInt(msg.length)
        out.writeCharSequence(msg, charset)
    }
}