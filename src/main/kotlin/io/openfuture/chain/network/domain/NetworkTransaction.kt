package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import java.nio.charset.StandardCharsets

class NetworkTransaction() : Packet() {

    var timestamp: Long = 0
    var amount: Double = 0.0
    lateinit var recipientAddress: String
    lateinit var senderKey: String
    lateinit var senderAddress: String
    lateinit var senderSignature: String
    lateinit var hash: String

    constructor(timestamp: Long, amount: Double, recipientAddress: String, senderKey: String, senderAddress: String,
                senderSignature: String, hash: String) : this() {
        this.timestamp = timestamp
        this.amount = amount
        this.recipientAddress = recipientAddress
        this.senderKey = senderKey
        this.senderAddress = senderAddress
        this.senderSignature = senderSignature
        this.hash = hash
    }

    override fun get(buffer: ByteBuf) {
        timestamp = buffer.readLong()
        amount = buffer.readDouble()
        var length = buffer.readInt()
        recipientAddress = buffer.readCharSequence(length, StandardCharsets.UTF_8).toString()
        length = buffer.readInt()
        senderKey = buffer.readCharSequence(length, StandardCharsets.UTF_8).toString()
        length = buffer.readInt()
        senderAddress = buffer.readCharSequence(length, StandardCharsets.UTF_8).toString()
        length = buffer.readInt()
        senderSignature = buffer.readCharSequence(length, StandardCharsets.UTF_8).toString()
        length = buffer.readInt()
        hash = buffer.readCharSequence(length, StandardCharsets.UTF_8).toString()
    }

    override fun send(buffer: ByteBuf) {
        buffer.writeLong(timestamp)
        buffer.writeDouble(amount)
        buffer.writeInt(senderKey.length)
        buffer.writeCharSequence(senderKey, StandardCharsets.UTF_8)
        buffer.writeInt(senderAddress.length)
        buffer.writeCharSequence(senderAddress, StandardCharsets.UTF_8)
        buffer.writeInt(senderSignature.length)
        buffer.writeCharSequence(senderSignature, StandardCharsets.UTF_8)
        buffer.writeInt(hash.length)
        buffer.writeCharSequence(hash, StandardCharsets.UTF_8)
    }

    override fun toString() = "Networktransaction(height=$timestamp)"

}