package io.openfuture.chain.network.message.sync

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readList
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeList
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.serialization.Serializable

@NoArgConstructor
data class SyncResponseMessage(
    var timestamp: Long,
    var blocksAfter: List<SyncBlockDto>
) : Serializable {

    override fun read(buf: ByteBuf) {
        timestamp = buf.readLong()
        blocksAfter = buf.readList()
    }

    override fun write(buf: ByteBuf) {
        buf.writeLong(timestamp)
        buf.writeList(blocksAfter)
    }

}

@NoArgConstructor
data class SyncBlockDto(
    var height: Long,
    var hash: String
) : Serializable {

    override fun read(buf: ByteBuf) {
        height = buf.readLong()
        hash = buf.readString()
    }

    override fun write(buf: ByteBuf) {
        buf.writeLong(height)
        buf.writeString(hash)
    }

}