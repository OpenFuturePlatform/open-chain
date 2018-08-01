package io.openfuture.chain.network.domain.base

import io.netty.buffer.ByteBuf
import java.io.Serializable

interface BaseMessage : Serializable {

    fun read(buffer: ByteBuf) {}

    fun write(buffer: ByteBuf) {}

}