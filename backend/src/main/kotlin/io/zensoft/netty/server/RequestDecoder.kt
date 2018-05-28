package io.zensoft.netty.server

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import java.nio.charset.Charset


class RequestDecoder : ByteToMessageDecoder() {

    private val charset = Charset.forName("UTF-8")

    @Throws(Exception::class)
    override fun decode(ctx: ChannelHandlerContext, `in`: ByteBuf, out: MutableList<Any>) {
        val length = `in`.readInt()
        val clientMessage = `in`.readCharSequence(length, charset)
        out.add(clientMessage)
    }
}