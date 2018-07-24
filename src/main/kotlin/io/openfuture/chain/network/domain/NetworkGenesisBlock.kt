package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.entity.GenesisBlock
import java.util.*

class NetworkGenesisBlock(height: Long = 0,
                          previousHash: String,
                          merkleHash: String,
                          timestamp: Long = 0,
                          typeId: Int = 0,
                          hash: String,
                          signature: String,
                          transactions: MutableList<NetworkTransaction>,
                          var epochIndex: Long,
                          var activeDelegates: MutableSet<NetworkDelegate>) :
    NetworkBlock(height, previousHash, merkleHash, timestamp, typeId, hash, signature, transactions) {

    constructor(block: GenesisBlock) : this(
        block.height,
        block.previousHash,
        block.merkleHash,
        block.timestamp,
        block.typeId,
        block.hash,
        block.signature!!,
        Collections.emptyList(),
        block.epochIndex,
        block.activeDelegates.map { NetworkDelegate(it) }.toMutableSet()
    )

    override fun get(buffer: ByteBuf) {
        super.get(buffer)

        epochIndex = buffer.readLong()

        val size = buffer.readInt()
        activeDelegates = mutableSetOf()
        for (index in 1..size) {
            val activeDelegate = NetworkDelegate()
            activeDelegate.get(buffer)
            activeDelegates.add(activeDelegate)
        }
    }

    override fun send(buffer: ByteBuf) {
        super.send(buffer)

        buffer.writeLong(epochIndex)
        buffer.writeInt(activeDelegates.size)
        for (activeDelegate in activeDelegates) {
            activeDelegate.send(buffer)
        }
    }

}
