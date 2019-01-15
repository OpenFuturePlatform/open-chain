package io.openfuture.chain.network.message.sync

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.core.sync.SyncMode
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.serialization.Serializable

@NoArgConstructor
class EpochRequestMessage(
    var epochIndex: Long,
    var syncMode: SyncMode
) : Serializable {

    override fun read(buf: ByteBuf) {
        epochIndex = buf.readLong()
        syncMode = SyncMode.valueOf(buf.readString())
    }

    override fun write(buf: ByteBuf) {
        buf.writeLong(epochIndex)
        buf.writeString(syncMode.toString())
    }

}