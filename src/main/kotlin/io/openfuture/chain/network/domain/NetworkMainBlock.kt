package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.annotation.NoArgConstructor
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.transaction.TransferTransaction
import io.openfuture.chain.entity.transaction.VoteTransaction

@NoArgConstructor
class NetworkMainBlock(height: Long = 0,
                       previousHash: String,
                       merkleHash: String,
                       blockTimestamp: Long = 0,
                       typeId: Int = 0,
                       hash: String,
                       signature: String,
                       var transferTransactions: MutableList<NetworkTransferTransaction>,
                       var voteTransactions: MutableList<NetworkVoteTransaction>) : NetworkBlock(height, previousHash, merkleHash, blockTimestamp, typeId, hash, signature) {

    constructor(block: MainBlock) : this(block.height, block.previousHash, block.merkleHash, block.timestamp,
        block.typeId, block.hash, block.signature!!,
        block.transactions.filterIsInstance(TransferTransaction::class.java).map { NetworkTransferTransaction(it) }.toMutableList(),
        block.transactions.filterIsInstance(VoteTransaction::class.java).map { NetworkVoteTransaction(it) }.toMutableList()) {
    }

    override fun readParams(buffer: ByteBuf) {
        super.readParams(buffer)

        var size = buffer.readInt()
        transferTransactions = mutableListOf()
        for (index in 1..size) {
            val transaction = NetworkTransferTransaction::class.java.newInstance()
            transaction.read(buffer)
            transferTransactions.add(transaction)
        }

        size = buffer.readInt()
        voteTransactions = mutableListOf()
        for (index in 1..size) {
            val transaction = NetworkVoteTransaction::class.java.newInstance()
            transaction.read(buffer)
            voteTransactions.add(transaction)
        }
    }

    override fun writeParams(buffer: ByteBuf) {
        super.writeParams(buffer)

        writeTransactions(buffer, transferTransactions)
        writeTransactions(buffer, voteTransactions)
    }

    private fun writeTransactions(buffer: ByteBuf, transactions: List<NetworkTransaction>) {
        buffer.writeInt(transactions.size)
        for (transaction in transactions) {
            transaction.write(buffer)
        }
    }

}
