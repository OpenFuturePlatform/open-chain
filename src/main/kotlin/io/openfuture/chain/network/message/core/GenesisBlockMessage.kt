package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readStringList
import io.openfuture.chain.network.extension.writeStringList

@NoArgConstructor
class GenesisBlockMessage(
    height: Long,
    previousHash: String,
    blockTimestamp: Long,
    reward: Long,
    publicKey: String,
    hash: String,
    signature: String,
    var epochIndex: Long,
    var activeDelegates: List<String>
) : BlockMessage(height, previousHash, blockTimestamp, reward, publicKey, hash, signature) {

    override fun read(buffer: ByteBuf) {
        super.read(buffer)

        epochIndex = buffer.readLong()
        activeDelegates = buffer.readStringList()
    }

    override fun write(buffer: ByteBuf) {
        super.write(buffer)

        buffer.writeLong(epochIndex)
        buffer.writeStringList(activeDelegates)
    }

    override fun toString() = "NetworkGenesisBlock(hash=$hash)"

}
