package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.network.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString

@NoArgConstructor
class DelegateTransactionMessage(
    timestamp: Long,
    fee: Long,
    senderAddress: String,
    senderPublicKey: String,
    senderSignature: String,
    hash: String,
    var delegateKey: String
) : BaseTransactionMessage(timestamp, fee, senderAddress, senderPublicKey, senderSignature, hash) {

    constructor(tx: DelegateTransaction) : this(
        tx.timestamp,
        tx.payload.fee,
        tx.senderAddress,
        tx.senderPublicKey,
        tx.senderSignature,
        tx.hash,
        tx.payload.delegateKey
    )

    override fun read(buffer: ByteBuf) {
        super.read(buffer)
        delegateKey = buffer.readString()
    }

    override fun write(buffer: ByteBuf) {
        super.write(buffer)
        buffer.writeString(delegateKey)
    }

}