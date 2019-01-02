package io.openfuture.chain.network.message.sync

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.serialization.Serializable

@NoArgConstructor
class EpochRequestMessage(
    var epochIndex: Long
) : Serializable {

    override fun read(buf: ByteBuf) {
        epochIndex = buf.readLong()
    }

    override fun write(buf: ByteBuf) {
        buf.writeLong(epochIndex)
    }

}