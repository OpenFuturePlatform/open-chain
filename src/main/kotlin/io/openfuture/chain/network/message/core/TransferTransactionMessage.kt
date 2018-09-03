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

    override fun read(buf: ByteBuf) {
        super.read(buf)
        amount = buf.readLong()
        recipientAddress = buf.readString()
    }

    override fun write(buf: ByteBuf) {
        super.write(buf)
        buf.writeLong(amount)
        buf.writeString(recipientAddress)
    }

}
