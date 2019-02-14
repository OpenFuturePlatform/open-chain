package io.openfuture.chain.network.message.base

import io.netty.buffer.ByteBuf

interface Message {

    fun read(buf: ByteBuf)

    fun write(buf: ByteBuf)

}