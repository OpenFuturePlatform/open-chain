package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.network.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString

@NoArgConstructor
class TransferTransactionMessage(
    timestamp: Long,
    fee: Long,
    senderAddress: String,
    senderPublicKey: String,
    senderSignature: String,
    hash: String,
    var amount: Long,
    var recipientAddress: String
) : BaseTransactionMessage(timestamp, fee, senderAddress, senderPublicKey, senderSignature, hash) {

    constructor(tx: TransferTransaction) : this(
        tx.timestamp,
        tx.payload.fee,
        tx.senderAddress,
        tx.senderPublicKey,
        tx.senderSignature,
        tx.hash,
        tx.payload.amount,
        tx.payload.recipientAddress
    )

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

}
