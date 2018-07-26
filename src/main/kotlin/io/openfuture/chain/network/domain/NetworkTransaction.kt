package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.annotation.NoArgConstructor

@NoArgConstructor
abstract class NetworkTransaction(var timestamp: Long,
                              var amount: Double,
                              var fee: Double,
                              var recipientAddress: String,
                              var senderKey: String,
                              var senderAddress: String,
                              var senderSignature: String,
                              var hash: String) : NetworkEntity() {

    override fun read(buffer: ByteBuf) {
        timestamp = buffer.readLong()
        amount = buffer.readDouble()
        fee = buffer.readDouble()
        recipientAddress = readString(buffer)
        senderKey = readString(buffer)
        senderAddress = readString(buffer)
        senderSignature = readString(buffer)
        hash = readString(buffer)
    }

    override fun write(buffer: ByteBuf) {
        buffer.writeLong(timestamp)
        buffer.writeDouble(amount)
        buffer.writeDouble(fee)
        writeString(buffer, recipientAddress)
        writeString(buffer, senderKey)
        writeString(buffer, senderAddress)
        writeString(buffer, senderSignature)
        writeString(buffer, hash)
    }

}