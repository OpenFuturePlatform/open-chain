package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString

class VoteTransactionMessage(
    timestamp: Long,
    fee: Long,
    senderAddress: String,
    hash: String,
    senderSignature: String,
    senderPublicKey: String,
    var voteTypeId: Int,
    var delegateKey: String
) : BaseTransactionMessage(timestamp, fee, senderAddress, hash, senderSignature, senderPublicKey) {

    constructor(tx: VoteTransaction) : this(
        tx.timestamp,
        tx.fee,
        tx.senderAddress,
        tx.hash,
        tx.senderSignature,
        tx.senderPublicKey,
        tx.payload.voteTypeId,
        tx.payload.delegateKey
    )

    override fun read(buffer: ByteBuf) {
        super.read(buffer)
        voteTypeId = buffer.readInt()
        delegateKey = buffer.readString()
    }

    override fun write(buffer: ByteBuf) {
        super.write(buffer)
        buffer.writeInt(voteTypeId)
        buffer.writeString(delegateKey)
    }

}