package io.openfuture.chain.network.message.application.transaction

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UDelegateTransaction
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

    constructor(tx: UDelegateTransaction) : this(
        tx.timestamp,
        1 /* TODO: replace after merging tx.getPayload().fee*/,
        tx.senderAddress,
        tx.senderPublicKey,
        tx.senderSignature,
        tx.hash,
        "delegateKey" /* TODO: replace after merging tx.getPayload().delegateKey*/
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