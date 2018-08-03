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
    var delegates: List<String>
) : BlockMessage(height, previousHash, timestamp, reward, hash, signature, publicKey) {

    constructor(block: GenesisBlock) : this(
        block.height,
        block.getPayload().previousHash,
        block.timestamp,
        block.getPayload().reward,
        block.hash,
        block.signature,
        block.publicKey,
        block.getPayload().epochIndex,
        block.getPayload().activeDelegates.map { it.publicKey }
    )

    override fun read(buffer: ByteBuf) {
        super.read(buffer)

        epochIndex = buffer.readLong()
//        delegates = buffer.readList<String>()
    }

    override fun write(buffer: ByteBuf) {
        super.write(buffer)

        buffer.writeLong(epochIndex)
//        buffer.writeList(delegates.toList())
    }

    override fun toString() = "NetworkGenesisBlock(hash=$hash)"

}
