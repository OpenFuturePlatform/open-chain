package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.entity.transaction.VoteTransaction
import java.nio.charset.StandardCharsets.UTF_8

class NetworkVoteTransaction(
    timestamp: Long,
    amount: Double,
    fee: Double,
    recipientAddress: String,
    senderKey: String,
    senderAddress: String,
    senderSignature: String,
    hash: String,

    var voteTypeId: Int,
    var delegateHost: String,
    var delegatePort: Int

) : NetworkTransaction(timestamp, amount, fee, recipientAddress, senderKey, senderAddress, senderSignature, hash) {

    constructor(transaction: VoteTransaction) : this(
        transaction.timestamp,
        transaction.amount,
        transaction.fee,
        transaction.recipientAddress,
        transaction.senderKey,
        transaction.senderAddress,
        transaction.senderSignature!!,
        transaction.hash,
        transaction.getVoteType().getId(),
        transaction.delegateHost,
        transaction.delegatePort
    )

    override fun get(buffer: ByteBuf) {
        super.get(buffer)

        voteTypeId = buffer.readInt()
        var length = buffer.readInt()
        delegateHost = buffer.readCharSequence(length, UTF_8).toString()
        delegatePort = buffer.readInt()
    }

    override fun send(buffer: ByteBuf) {
        super.send(buffer)

        buffer.writeInt(voteTypeId)
        buffer.writeCharSequence(delegateHost, UTF_8)
        buffer.writeInt(delegatePort)
    }

}
