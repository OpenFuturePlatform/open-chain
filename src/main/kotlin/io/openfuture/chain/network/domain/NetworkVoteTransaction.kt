package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.entity.transaction.VoteTransaction
import java.nio.charset.StandardCharsets.UTF_8

class NetworkVoteTransaction() : NetworkTransaction() {

    var voteTypeId: Int = 0
    lateinit var delegateHost: String
    var delegatePort: Int = 0


    constructor(transaction: VoteTransaction) : this() {
        timestamp = transaction.timestamp
        amount = transaction.amount
        fee = transaction.fee
        recipientAddress = transaction.recipientAddress
        senderKey = transaction.senderKey
        senderAddress = transaction.senderAddress
        senderSignature = transaction.senderSignature!!
        hash = transaction.hash
        voteTypeId = transaction.getVoteType().getId()
        delegateHost = transaction.delegateHost
        delegatePort = transaction.delegatePort
    }

    override fun get(buffer: ByteBuf) {
        super.get(buffer)

        voteTypeId = buffer.readInt()
        delegateHost = buffer.readCharSequence(buffer.readInt(), UTF_8).toString()
        delegatePort = buffer.readInt()
    }

    override fun send(buffer: ByteBuf) {
        super.send(buffer)

        buffer.writeInt(voteTypeId)
        buffer.writeInt(delegateHost.length)
        buffer.writeCharSequence(delegateHost, UTF_8)
        buffer.writeInt(delegatePort)
    }

}
