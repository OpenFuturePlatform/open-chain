package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.annotation.NoArgConstructor
import io.openfuture.chain.entity.GenesisBlock

@NoArgConstructor
class NetworkGenesisBlock(height: Long = 0,
                          previousHash: String,
                          merkleHash: String,
                          blockTimestamp: Long = 0,
                          typeId: Int = 0,
                          hash: String,
                          signature: String,
                          var epochIndex: Long = 0,
                          var activeDelegates: MutableSet<NetworkDelegate>
) : NetworkBlock(height, previousHash, merkleHash, blockTimestamp, typeId, hash, signature) {


    constructor(block: GenesisBlock) : this(block.height, block.previousHash, block.merkleHash, block.timestamp,
        block.typeId, block.hash, block.signature!!, block.epochIndex, block.activeDelegates.map { NetworkDelegate(it) }.toMutableSet())

    override fun readParams(buffer: ByteBuf) {
        super.readParams(buffer)

        epochIndex = buffer.readLong()
        val size = buffer.readInt()
        activeDelegates = mutableSetOf()
        for (index in 1..size) {
            val address = NetworkDelegate::class.java.newInstance()
            address.read(buffer)
            activeDelegates.add(address)
        }
    }

    override fun writeParams(buffer: ByteBuf) {
        super.writeParams(buffer)

        buffer.writeLong(epochIndex)

        buffer.writeInt(activeDelegates.size)
        for (delegate in activeDelegates) {
            delegate.write(buffer)
        }
    }

}
