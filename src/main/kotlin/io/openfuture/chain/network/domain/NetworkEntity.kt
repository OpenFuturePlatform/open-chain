package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import java.io.Serializable

@NoArgConstructor
abstract class NetworkEntity : Serializable {

    abstract fun read(buffer: ByteBuf)

    abstract fun write(buffer: ByteBuf)

}