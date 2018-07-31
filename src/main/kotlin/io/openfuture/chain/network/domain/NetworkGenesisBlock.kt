package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.consensus.annotation.NoArgConstructor
import io.openfuture.chain.consensus.model.entity.Delegate
import io.openfuture.chain.consensus.model.entity.block.GenesisBlock
import io.openfuture.chain.network.extension.readList
import io.openfuture.chain.network.extension.writeList

@NoArgConstructor
class NetworkGenesisBlock(
    height: Long,
    previousHash: String,
    blockTimestamp: Long,
    reward: Long,
    publicKey: String,
    hash: String,
    signature: String,
    var epochIndex: Long,
    var activeDelegates: MutableSet<NetworkDelegate>
) : NetworkBlock(height, previousHash, blockTimestamp, reward, publicKey, hash, signature) {
    
    constructor(block: GenesisBlock) : this(
        block.height,
        block.previousHash,
        block.timestamp,
        block.reward,
        block.publicKey,
        block.hash,
        block.signature!!,
        block.epochIndex,
        block.activeDelegates.map { NetworkDelegate(it) }.toMutableSet())

    override fun readParams(buffer: ByteBuf) {
        super.readParams(buffer)

        epochIndex = buffer.readLong()
        activeDelegates = buffer.readList<NetworkDelegate>().toMutableSet()
    }

    override fun writeParams(buffer: ByteBuf) {
        super.writeParams(buffer)

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
