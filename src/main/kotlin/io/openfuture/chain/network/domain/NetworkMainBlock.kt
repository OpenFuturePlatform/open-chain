package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.transaction.TransferTransaction
import io.openfuture.chain.entity.transaction.VoteTransaction

class NetworkMainBlock() : NetworkBlock() {

    lateinit var transferTransactions: MutableList<NetworkTransferTransaction>
    lateinit var voteTransactions: MutableList<NetworkVoteTransaction>

    constructor(block: MainBlock) : this() {
        this.height = block.height
        this.previousHash = block.previousHash
        this.merkleHash = block.merkleHash
        this.timestamp = block.timestamp
        this.typeId = block.typeId
        this.hash = block.hash
        this.signature = block.signature!!
        this.transferTransactions = block.transactions.filterIsInstance(TransferTransaction::class.java)
            .map { NetworkTransferTransaction(it) }.toMutableList()
        this.voteTransactions = block.transactions.filterIsInstance(VoteTransaction::class.java).map { NetworkVoteTransaction(it) }.toMutableList()
    }


    override fun get(buffer: ByteBuf) {
        super.get(buffer)

        var size = buffer.readInt()
        transferTransactions = mutableListOf()
        for (index in 1..size) {
            val transaction = NetworkTransferTransaction()
            transaction.get(buffer)
            transferTransactions.add(transaction)
        }

        size = buffer.readInt()
        voteTransactions = mutableListOf()
        for (index in 1..size) {
            val transaction = NetworkVoteTransaction()
            transaction.get(buffer)
            voteTransactions.add(transaction)
        }
    }

    override fun send(buffer: ByteBuf) {
        super.send(buffer)

        sendTransactions(buffer, transferTransactions)
        sendTransactions(buffer, voteTransactions)
    }

    private fun sendTransactions(buffer: ByteBuf, transactions: List<NetworkTransaction>) {
        buffer.writeInt(transactions.size)
        for (transaction in transactions) {
            transaction.send(buffer)
        }
    }

}
