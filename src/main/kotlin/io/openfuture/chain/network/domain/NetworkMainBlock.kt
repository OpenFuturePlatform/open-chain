package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.annotation.NoArgConstructor
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.transaction.TransferTransaction
import io.openfuture.chain.entity.transaction.VoteTransaction

@NoArgConstructor
class NetworkMainBlock(height: Long,
                       previousHash: String,
                       merkleHash: String,
                       blockTimestamp: Long,
                       typeId: Int,
                       hash: String,
                       signature: String,
                       var transferTransactions: MutableList<NetworkTransferTransaction>,
                       var voteTransactions: MutableList<NetworkVoteTransaction>) :
    NetworkBlock(height, previousHash, merkleHash, blockTimestamp, typeId, hash, signature) {

    constructor(block: MainBlock) : this(block.height, block.previousHash, block.merkleHash, block.timestamp,
        block.typeId, block.hash, block.signature!!,
        block.transactions.filterIsInstance(TransferTransaction::class.java).map { NetworkTransferTransaction(it) }.toMutableList(),
        block.transactions.filterIsInstance(VoteTransaction::class.java).map { NetworkVoteTransaction(it) }.toMutableList())

    override fun readParams(buffer: ByteBuf) {
        super.readParams(buffer)

        transferTransactions = readList(buffer)
        voteTransactions = readList(buffer)
    }

    override fun writeParams(buffer: ByteBuf) {
        super.writeParams(buffer)

        writeList(buffer, transferTransactions)
        writeList(buffer, voteTransactions)
    }

    override fun toString() = "NetworkMainBlock(hash=$hash)"

}
