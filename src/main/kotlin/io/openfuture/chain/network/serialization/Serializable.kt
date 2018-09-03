package io.openfuture.chain.network.serialization

import io.netty.buffer.ByteBuf

interface Serializable {

    fun read(buf: ByteBuf)

    fun write(buf: ByteBuf)

}