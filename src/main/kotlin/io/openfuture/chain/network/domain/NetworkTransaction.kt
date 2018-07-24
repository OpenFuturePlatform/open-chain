package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import java.nio.charset.StandardCharsets

open class NetworkTransaction : Packet() {

    var timestamp: Long = 0
    var amount: Double = 0.0
    var fee: Double = 0.0
    lateinit var recipientAddress: String
    lateinit var senderKey: String
    lateinit var senderAddress: String
    lateinit var senderSignature: String
    lateinit var hash: String

    override fun get(buffer: ByteBuf) {
        timestamp = buffer.readLong()
        amount = buffer.readDouble()
        fee = buffer.readDouble()
        recipientAddress = buffer.readCharSequence(buffer.readInt(), StandardCharsets.UTF_8).toString()
        senderKey = buffer.readCharSequence(buffer.readInt(), StandardCharsets.UTF_8).toString()
        senderAddress = buffer.readCharSequence(buffer.readInt(), StandardCharsets.UTF_8).toString()
        senderSignature = buffer.readCharSequence(buffer.readInt(), StandardCharsets.UTF_8).toString()
        hash = buffer.readCharSequence(buffer.readInt(), StandardCharsets.UTF_8).toString()
    }

    override fun send(buffer: ByteBuf) {
        buffer.writeLong(timestamp)
        buffer.writeDouble(amount)
        buffer.writeDouble(fee)
        buffer.writeInt(recipientAddress.length)
        buffer.writeCharSequence(recipientAddress, StandardCharsets.UTF_8)
        buffer.writeInt(senderKey.length)
        buffer.writeCharSequence(senderKey, StandardCharsets.UTF_8)
        buffer.writeInt(senderAddress.length)
        buffer.writeCharSequence(senderAddress, StandardCharsets.UTF_8)
        buffer.writeInt(senderSignature.length)
        buffer.writeCharSequence(senderSignature, StandardCharsets.UTF_8)
        buffer.writeInt(hash.length)
        buffer.writeCharSequence(hash, StandardCharsets.UTF_8)
    }

    override fun toString() = "NetworkTransaction(hash=$hash)"

}