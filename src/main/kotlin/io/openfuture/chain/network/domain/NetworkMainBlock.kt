package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.openfuture.chain.annotation.NoArgConstructor
import io.openfuture.chain.domain.transaction.DelegateTransactionDto
import io.openfuture.chain.domain.transaction.RewardTransactionDto
import io.openfuture.chain.domain.transaction.TransferTransactionDto
import io.openfuture.chain.domain.transaction.VoteTransactionDto
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.transaction.DelegateTransaction
import io.openfuture.chain.entity.transaction.RewardTransaction
import io.openfuture.chain.entity.transaction.TransferTransaction
import io.openfuture.chain.entity.transaction.VoteTransaction
import io.openfuture.chain.network.extension.readList
import io.openfuture.chain.network.extension.writeList
import java.util.*

@NoArgConstructor
class NetworkMainBlock(height: Long,
                       previousHash: String,
                       merkleHash: String,
                       blockTimestamp: Long,
                       typeId: Int,
                       hash: String,
                       signature: String,
                       var transferTransactions: MutableList<TransferTransactionDto>,
                       var voteTransactions: MutableList<VoteTransactionDto>,
                       var delegateTransactions: MutableList<DelegateTransactionDto>,
                       var rewardTransactions: MutableList<RewardTransactionDto>) :
    NetworkBlock(height, previousHash, merkleHash, blockTimestamp, typeId, hash, signature) {

    constructor(block: MainBlock) : this(block.height, block.previousHash, block.merkleHash, block.timestamp,
        block.typeId, block.hash, block.signature!!,
        block.transactions.filterIsInstance(TransferTransaction::class.java).map { TransferTransactionDto(it) }.toMutableList(),
        block.transactions.filterIsInstance(VoteTransaction::class.java).map { VoteTransactionDto(it) }.toMutableList(),
        block.transactions.filterIsInstance(DelegateTransaction::class.java).map { DelegateTransactionDto(it) }.toMutableList(),
        block.transactions.filterIsInstance(RewardTransaction::class.java).map { RewardTransactionDto(it) }.toMutableList())

    override fun readParams(buffer: ByteBuf) {
        super.readParams(buffer)

        transferTransactions = buffer.readList()
        voteTransactions = buffer.readList()
        delegateTransactions = buffer.readList()
        rewardTransactions = buffer.readList()
    }

    override fun writeParams(buffer: ByteBuf) {
        super.writeParams(buffer)

        buffer.writeList(transferTransactions)
        buffer.writeList(voteTransactions)
        buffer.writeList(delegateTransactions)
        buffer.writeList(rewardTransactions)
    }

    fun toEntity(): MainBlock = MainBlock( //todo wait to realize block dto entity converter ?
        height,
        previousHash,
        merkleHash,
        blockTimestamp,
        Collections.emptyList()).apply { signature = super.signature }


    override fun toString() = "NetworkMainBlock(hash=$hash)"

}
