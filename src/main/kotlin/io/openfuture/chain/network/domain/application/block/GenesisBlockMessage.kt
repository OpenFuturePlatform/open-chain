package io.openfuture.chain.network.domain.application.block

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.network.domain.BlockMessage
import io.openfuture.chain.network.domain.application.delegate.DelegateMessage
import io.openfuture.chain.network.extension.readList
import io.openfuture.chain.network.extension.writeList

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
    var activeDelegates: MutableSet<DelegateMessage>
) : BlockMessage(height, previousHash, blockTimestamp, reward, publicKey, hash, signature) {
    
    constructor(block: GenesisBlock) : this(
        block.height,
        block.previousHash,
        block.timestamp,
        block.reward,
        block.publicKey,
        block.hash,
        block.signature!!,
        block.epochIndex,
        block.activeDelegates.map { DelegateMessage(it) }.toMutableSet())

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

    fun toEntity(): GenesisBlock = GenesisBlock(
        height,
        previousHash,
        blockTimestamp,
        reward,
        publicKey,
        epochIndex,
        activeDelegates.map { Delegate.of(it) }.toMutableSet()
    ).apply { signature = super.signature }

    override fun toString() = "NetworkGenesisBlock(hash=$hash)"

}
