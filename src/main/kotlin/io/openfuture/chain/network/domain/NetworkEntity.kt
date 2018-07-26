package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import java.io.Serializable

abstract class NetworkEntity() : Serializable {

    abstract fun read(buffer: ByteBuf)

    abstract fun write(buffer: ByteBuf)

}