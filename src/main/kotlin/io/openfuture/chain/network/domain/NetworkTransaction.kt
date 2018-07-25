package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.annotation.NoArgConstructor
import java.nio.charset.StandardCharsets.UTF_8

@NoArgConstructor
abstract class NetworkTransaction(var timestamp: Long = 0,
                              var amount: Double = 0.0,
                              var fee: Double = 0.0,
                              var recipientAddress: String,
                              var senderKey: String,
                              var senderAddress: String,
                              var senderSignature: String,
                              var hash: String) {

    open fun read(buffer: ByteBuf) {
        timestamp = buffer.readLong()
        amount = buffer.readDouble()
        fee = buffer.readDouble()
        recipientAddress = buffer.readCharSequence(buffer.readInt(), UTF_8).toString()
        senderKey = buffer.readCharSequence(buffer.readInt(), UTF_8).toString()
        senderAddress = buffer.readCharSequence(buffer.readInt(), UTF_8).toString()
        senderSignature = buffer.readCharSequence(buffer.readInt(), UTF_8).toString()
        hash = buffer.readCharSequence(buffer.readInt(), UTF_8).toString()
    }

    open fun write(buffer: ByteBuf) {
        buffer.writeLong(timestamp)
        buffer.writeDouble(amount)
        buffer.writeDouble(fee)
        buffer.writeInt(recipientAddress.length)
        buffer.writeCharSequence(recipientAddress, UTF_8)
        buffer.writeInt(senderKey.length)
        buffer.writeCharSequence(senderKey, UTF_8)
        buffer.writeInt(senderAddress.length)
        buffer.writeCharSequence(senderAddress, UTF_8)
        buffer.writeInt(senderSignature.length)
        buffer.writeCharSequence(senderSignature, UTF_8)
        buffer.writeInt(hash.length)
        buffer.writeCharSequence(hash, UTF_8)
    }

    override fun toString() = "NetworkTransaction(hash=$hash)"

}