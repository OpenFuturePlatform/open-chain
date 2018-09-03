package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString

@NoArgConstructor
class TransferTransactionMessage(
    timestamp: Long,
    fee: Long,
    senderAddress: String,
    hash: String,
    senderSignature: String,
    senderPublicKey: String,
    var amount: Long,
    var recipientAddress: String
) : TransactionMessage(timestamp, fee, senderAddress, hash, senderSignature, senderPublicKey) {

    override fun read(buffer: ByteBuf) {
        super.read(buffer)
        amount = buffer.readLong()
        recipientAddress = buffer.readString()
    }

    override fun write(buffer: ByteBuf) {
        super.write(buffer)
        buffer.writeLong(amount)
        buffer.writeString(recipientAddress)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TransferTransactionMessage) return false
        if (!super.equals(other)) return false

        if (amount != other.amount) return false
        if (recipientAddress != other.recipientAddress) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + amount.hashCode()
        result = 31 * result + recipientAddress.hashCode()
        return result
    }

}
