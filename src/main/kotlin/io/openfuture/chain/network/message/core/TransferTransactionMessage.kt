package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readNullableString
import io.openfuture.chain.network.extension.writeNullableString

@NoArgConstructor
class TransferTransactionMessage(
    timestamp: Long,
    fee: Long,
    senderAddress: String,
    hash: String,
    signature: String,
    publicKey: String,
    var amount: Long,
    var recipientAddress: String?,
    var data: String?
) : TransactionMessage(timestamp, fee, senderAddress, hash, signature, publicKey) {

    override fun read(buf: ByteBuf) {
        super.read(buf)
        amount = buf.readLong()
        recipientAddress = buf.readNullableString()
        data = buf.readNullableString()
    }

    override fun write(buf: ByteBuf) {
        super.write(buf)
        buf.writeLong(amount)
        buf.writeNullableString(recipientAddress)
        buf.writeNullableString(data)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TransferTransactionMessage) return false
        if (!super.equals(other)) return false

        if (amount != other.amount) return false
        if (recipientAddress != other.recipientAddress) return false
        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + amount.hashCode()
        result = 31 * result + (recipientAddress?.hashCode() ?: 0)
        result = 31 * result + (data?.hashCode() ?: 0)
        return result
    }

}
