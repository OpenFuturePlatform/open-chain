package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.entity.transaction.VoteTransaction

class NetworkVoteTransaction(timestamp: Long,
                             amount: Double,
                             fee: Double,
                             recipientAddress: String,
                             senderKey: String,
                             senderAddress: String,
                             senderSignature: String,
                             hash: String,
                             var voteTypeId: Int,
                             var delegateHost: String,
                             var delegatePort: Int) : NetworkTransaction(timestamp, amount, fee, recipientAddress,
    senderKey, senderAddress, senderSignature, hash) {

    constructor(transaction: VoteTransaction) : this(transaction.timestamp, transaction.amount,
        transaction.fee, transaction.recipientAddress, transaction.senderKey, transaction.senderAddress,
        transaction.senderSignature!!, transaction.hash, transaction.getVoteType().getId(),
        transaction.delegateHost, transaction.delegatePort)

    override fun read(buffer: ByteBuf) {
        super.read(buffer)

        voteTypeId = buffer.readInt()
        delegateHost = readString(buffer)
        delegatePort = buffer.readInt()
    }

    override fun write(buffer: ByteBuf) {
        super.write(buffer)

        buffer.writeInt(voteTypeId)
        writeString(buffer, delegateHost)
        buffer.writeInt(delegatePort)
    }

}
