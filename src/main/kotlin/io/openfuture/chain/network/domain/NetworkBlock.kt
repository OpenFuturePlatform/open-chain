package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import java.nio.charset.StandardCharsets
import java.nio.charset.StandardCharsets.UTF_8

class NetworkBlock() : Packet() {
    var height: Long = 0
    lateinit var previousHash: String
    lateinit var merkleHash: String
    var timestamp: Long = 0
    var typeId: Int = 0
    lateinit var hash: String
    lateinit var signature: String

    lateinit var transactions : MutableList<NetworkTransaction>


    constructor(height: Long, previousHash: String, merkleHash: String, timestamp: Long, typeId: Int, hash: String,
                signature: String, transactions: MutableList<NetworkTransaction>) : this() {
        this.height = height
        this.previousHash = previousHash
        this.merkleHash = merkleHash
        this.timestamp = timestamp
        this.typeId = typeId
        this.hash = hash
        this.signature = signature
        this.transactions = transactions
    }

    override fun get(buffer: ByteBuf) {
        height = buffer.readLong()
        var length = buffer.readInt()
        previousHash = buffer.readCharSequence(length, UTF_8).toString()
        length = buffer.readInt()
        merkleHash = buffer.readCharSequence(length, UTF_8).toString()
        timestamp = buffer.readLong()
        typeId = buffer.readInt()
        length = buffer.readInt()
        hash = buffer.readCharSequence(length, UTF_8).toString()
        length = buffer.readInt()
        signature = buffer.readCharSequence(length, UTF_8).toString()

        val size = buffer.readInt()
        transactions = mutableListOf()
        for (index in 1..size) {
            val transaction = NetworkTransaction()
            transaction.get(buffer)
            transactions.add(transaction)
        }
    }

    override fun send(buffer: ByteBuf) {
        buffer.writeLong(height)
        buffer.writeInt(previousHash.length)
        buffer.writeCharSequence(previousHash, StandardCharsets.UTF_8)
        buffer.writeInt(merkleHash.length)
        buffer.writeCharSequence(merkleHash, StandardCharsets.UTF_8)
        buffer.writeLong(timestamp)
        buffer.writeInt(typeId)
        buffer.writeInt(hash.length)
        buffer.writeCharSequence(hash, StandardCharsets.UTF_8)
        buffer.writeInt(signature.length)
        buffer.writeCharSequence(signature, StandardCharsets.UTF_8)

        buffer.writeInt(transactions.size)
        for (transaction in transactions) {
            transaction.send(buffer)
        }
    }

    override fun toString() = "NetworkBlock(height=$height)"

}