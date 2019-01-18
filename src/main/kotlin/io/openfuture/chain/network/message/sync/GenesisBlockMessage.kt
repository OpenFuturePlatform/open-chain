package io.openfuture.chain.network.message.sync

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readStringList
import io.openfuture.chain.network.extension.writeStringList
import io.openfuture.chain.network.message.core.BlockMessage

@NoArgConstructor
class GenesisBlockMessage(
    height: Long,
    previousHash: String,
    timestamp: Long,
    hash: String,
    signature: String,
    publicKey: String,
    var epochIndex: Long,
    var delegates: List<String>
) : BlockMessage(height, previousHash, timestamp, hash, signature, publicKey) {

    override fun read(buf: ByteBuf) {
        super.read(buf)

        epochIndex = buf.readLong()
        delegates = buf.readStringList()
    }

    override fun write(buf: ByteBuf) {
        super.write(buf)

        buf.writeLong(epochIndex)
        buf.writeStringList(delegates)
    }

}
