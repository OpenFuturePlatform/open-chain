package io.zensoft.netty.client

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import java.nio.charset.Charset


class ResponseDataDecoder : ByteToMessageDecoder() {

    private val charset = Charset.forName("UTF-8")

    @Throws(Exception::class)
    override fun decode(ctx: ChannelHandlerContext, `in`: ByteBuf, out: MutableList<Any>) {
        val length = `in`.readInt()
        val serverMessage = `in`.readCharSequence(length, charset).toString()
        out.add(serverMessage)
    }
}