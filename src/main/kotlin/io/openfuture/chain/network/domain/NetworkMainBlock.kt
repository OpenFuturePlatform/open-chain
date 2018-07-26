package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.annotation.NoArgConstructor
import io.openfuture.chain.domain.transaction.TransferTransactionDto
import io.openfuture.chain.domain.transaction.VoteTransactionDto
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.transaction.TransferTransaction
import io.openfuture.chain.entity.transaction.VoteTransaction
import io.openfuture.chain.network.extension.readList
import io.openfuture.chain.network.extension.writeList

@NoArgConstructor
class NetworkMainBlock(height: Long,
                       previousHash: String,
                       merkleHash: String,
                       blockTimestamp: Long,
                       typeId: Int,
                       hash: String,
                       signature: String,
                       var transferTransactions: MutableList<TransferTransactionDto>,
                       var voteTransactions: MutableList<VoteTransactionDto>) :
    NetworkBlock(height, previousHash, merkleHash, blockTimestamp, typeId, hash, signature) {

    constructor(block: MainBlock) : this(block.height, block.previousHash, block.merkleHash, block.timestamp,
        block.typeId, block.hash, block.signature!!,
        block.transactions.filterIsInstance(TransferTransaction::class.java).map { TransferTransactionDto(it) }.toMutableList(),
        block.transactions.filterIsInstance(VoteTransaction::class.java).map { VoteTransactionDto(it) }.toMutableList())

    override fun readParams(buffer: ByteBuf) {
        super.readParams(buffer)

        transferTransactions = buffer.readList()
        voteTransactions = buffer.readList()
    }

    override fun writeParams(buffer: ByteBuf) {
        super.writeParams(buffer)

        buffer.writeList(transferTransactions)
        buffer.writeList(voteTransactions)
    }

    override fun toString() = "NetworkMainBlock(hash=$hash)"

}
