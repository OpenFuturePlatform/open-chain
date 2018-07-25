package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.entity.transaction.VoteTransaction
import java.nio.charset.StandardCharsets.UTF_8

class NetworkVoteTransaction(timestamp: Long = 0,
                             amount: Double = 0.0,
                             fee: Double = 0.0,
                             recipientAddress: String,
                             senderKey: String,
                             senderAddress: String,
                             senderSignature: String,
                             hash: String,
                             var voteTypeId: Int = 0,
                             var delegateHost: String,
                             var delegatePort: Int = 0) : NetworkTransaction(timestamp, amount, fee, recipientAddress,
    senderKey, senderAddress, senderSignature, hash) {

    constructor(transaction: VoteTransaction) : this(transaction.timestamp, transaction.amount,
        transaction.fee, transaction.recipientAddress, transaction.senderKey, transaction.senderAddress,
        transaction.senderSignature!!, transaction.hash, transaction.getVoteType().getId(),
        transaction.delegateHost, transaction.delegatePort)

    override fun read(buffer: ByteBuf) {
        super.read(buffer)

        voteTypeId = buffer.readInt()
        delegateHost = buffer.readCharSequence(buffer.readInt(), UTF_8).toString()
        delegatePort = buffer.readInt()
    }

    override fun write(buffer: ByteBuf) {
        super.write(buffer)

        buffer.writeInt(voteTypeId)
        buffer.writeInt(delegateHost.length)
        buffer.writeCharSequence(delegateHost, UTF_8)
        buffer.writeInt(delegatePort)
    }

}
