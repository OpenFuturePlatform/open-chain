package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDelegateTransaction
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString

@NoArgConstructor
class DelegateTransactionMessage(
    timestamp: Long,
    fee: Long,
    senderAddress: String,
    hash: String,
    senderSignature: String,
    senderPublicKey: String,

    var delegateKey: String
) : BaseTransactionMessage(timestamp, fee, senderAddress, hash, senderSignature, senderPublicKey) {

    constructor(tx: DelegateTransaction) : this(
        tx.timestamp,
        tx.fee,
        tx.senderAddress,
        tx.hash,
        tx.senderSignature,
        tx.senderPublicKey,
        tx.payload.delegateKey
    )

    constructor(utx: UnconfirmedDelegateTransaction) : this(
        utx.timestamp,
        utx.fee,
        utx.senderAddress,
        utx.hash,
        utx.senderSignature,
        utx.senderPublicKey,
        utx.payload.delegateKey
    )

    override fun read(buffer: ByteBuf) {
        super.read(buffer)
        delegateKey = buffer.readString()
    }

    override fun write(buffer: ByteBuf) {
        super.write(buffer)
        buffer.writeString(delegateKey)
    }

    override fun toString() = "DelegateTransactionMessage($timestamp," +
        "$fee,$senderAddress,$hash,$senderSignature,$senderPublicKey,$delegateKey)"

}