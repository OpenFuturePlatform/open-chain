package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.network.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readList
import io.openfuture.chain.network.extension.writeList

@NoArgConstructor
class GenesisBlockMessage(
    height: Long,
    previousHash: String,
    timestamp: Long,
    reward: Long,
    hash: String,
    signature: String,
    publicKey: String,
    var epochIndex: Long,
    var activeDelegates: MutableSet<DelegateMessage>
) : BlockMessage(height, previousHash, timestamp, reward, hash, signature, publicKey) {

    constructor(block: GenesisBlock) : this(
        block.height,
        block.payload.previousHash,
        block.timestamp,
        block.payload.reward,
        block.hash,
        block.signature,
        block.publicKey,
        block.payload.epochIndex,
        block.payload.activeDelegates.map { DelegateMessage(it) }.toMutableSet()
    )

    override fun read(buffer: ByteBuf) {
        super.read(buffer)

        epochIndex = buffer.readLong()
        activeDelegates = buffer.readList<DelegateMessage>().toMutableSet()
    }

    override fun write(buffer: ByteBuf) {
        super.write(buffer)

        buffer.writeLong(epochIndex)
        buffer.writeList(activeDelegates.toList())
    }

    override fun toString() = "NetworkGenesisBlock(hash=$hash)"

}
