package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString

@NoArgConstructor
class DelegateTransactionMessage(
    timestamp: Long,
    fee: Long,
    senderAddress: String,
    hash: String,
    signature: String,
    publicKey: String,
    var delegateKey: String,
    var amount: Long
) : TransactionMessage(timestamp, fee, senderAddress, hash, signature, publicKey) {

    override fun read(buf: ByteBuf) {
        super.read(buf)
        delegateKey = buf.readString()
        amount = buf.readLong()
    }

    override fun write(buf: ByteBuf) {
        super.write(buf)
        buf.writeString(delegateKey)
        buf.writeLong(amount)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DelegateTransactionMessage) return false
        if (!super.equals(other)) return false

        if (delegateKey != other.delegateKey) return false
        if (amount != other.amount) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + delegateKey.hashCode()
        result = 31 * result + amount.hashCode()
        return result
    }

}