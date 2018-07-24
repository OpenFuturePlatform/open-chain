package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.entity.GenesisBlock

class NetworkGenesisBlock() : NetworkBlock() {

    var epochIndex: Long = 0
    lateinit var activeDelegates: MutableSet<NetworkDelegate>


    constructor(block: GenesisBlock) : this() {
        this.height = block.height
        this.previousHash = block.previousHash
        this.merkleHash = block.merkleHash
        this.timestamp = block.timestamp
        this.typeId = block.typeId
        this.hash = block.hash
        this.signature = block.signature!!
        this.epochIndex = block.epochIndex
        this.activeDelegates = block.activeDelegates.map { NetworkDelegate(it) }.toMutableSet()
    }

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
