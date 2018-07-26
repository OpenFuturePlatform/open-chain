package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.annotation.NoArgConstructor
import io.openfuture.chain.entity.GenesisBlock
import io.openfuture.chain.network.extension.readList
import io.openfuture.chain.network.extension.writeList

@NoArgConstructor
class NetworkGenesisBlock(height: Long,
                          previousHash: String,
                          merkleHash: String,
                          blockTimestamp: Long,
                          typeId: Int,
                          hash: String,
                          signature: String,
                          var epochIndex: Long,
                          var activeDelegates: MutableSet<NetworkDelegate>
) : NetworkBlock(height, previousHash, merkleHash, blockTimestamp, typeId, hash, signature) {


    constructor(block: GenesisBlock) : this(block.height, block.previousHash, block.merkleHash, block.timestamp,
        block.typeId, block.hash, block.signature!!, block.epochIndex, block.activeDelegates.map { NetworkDelegate(it) }.toMutableSet())

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

    override fun toString() = "NetworkGenesisBlock(hash=$hash)"

}
