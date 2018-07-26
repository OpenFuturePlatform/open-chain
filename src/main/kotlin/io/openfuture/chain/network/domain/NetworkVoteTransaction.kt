package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.entity.transaction.VoteTransaction
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString

class NetworkVoteTransaction(timestamp: Long,
                             amount: Long,
                             fee: Long,
                             recipientAddress: String,
                             senderKey: String,
                             senderAddress: String,
                             senderSignature: String,
                             hash: String,
                             var voteTypeId: Int,
                             var delegateKey: String) : NetworkTransaction(timestamp, amount, fee, recipientAddress,
    senderKey, senderAddress, senderSignature, hash) {

    constructor(transaction: VoteTransaction) : this(transaction.timestamp, transaction.amount,
        transaction.fee, transaction.recipientAddress, transaction.senderPublicKey, transaction.senderAddress,
        transaction.senderSignature, transaction.hash, transaction.getVoteType().getId(),
        transaction.delegateKey)

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
