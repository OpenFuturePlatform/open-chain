package io.openfuture.chain.network.message.sync

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.serialization.Serializable

@NoArgConstructor
data class SyncResponseMessage(
    var timestamp: Long,
    var lastBlockHash: String,
    var lastBlockHeight: Long
) : Serializable {

    override fun read(buf: ByteBuf) {
        timestamp = buf.readLong()
        lastBlockHash = buf.readString()
        lastBlockHeight = buf.readLong()
    }

    override fun write(buf: ByteBuf) {
        buf.writeLong(timestamp)
        buf.writeString(lastBlockHash)
        buf.writeLong(lastBlockHeight)
    }

}