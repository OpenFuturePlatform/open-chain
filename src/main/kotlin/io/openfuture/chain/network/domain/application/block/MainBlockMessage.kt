package io.openfuture.chain.network.domain.application.block

import io.netty.buffer.ByteBuf
import io.openfuture.chain.annotation.NoArgConstructor
import io.openfuture.chain.network.domain.application.transaction.DelegateTransactionMessage
import io.openfuture.chain.network.domain.application.transaction.RewardTransactionMessage
import io.openfuture.chain.network.domain.application.transaction.TransferTransactionMessage
import io.openfuture.chain.network.domain.application.transaction.VoteTransactionMessage
import io.openfuture.chain.entity.block.MainBlock
import io.openfuture.chain.entity.transaction.DelegateTransaction
import io.openfuture.chain.entity.transaction.RewardTransaction
import io.openfuture.chain.entity.transaction.TransferTransaction
import io.openfuture.chain.entity.transaction.VoteTransaction
import io.openfuture.chain.network.extension.readList
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeList
import io.openfuture.chain.network.extension.writeString

@NoArgConstructor
class MainBlockMessage(
    height: Long,
    previousHash: String,
    blockTimestamp: Long,
    hash: String,
    signature: String,
    publicKey: String,
    var merkleHash: String,
    var transferTransactions: MutableList<TransferTransactionMessage>,
    var voteTransactions: MutableList<VoteTransactionMessage>,
    var delegateTransactions: MutableList<DelegateTransactionMessage>,
    var rewardTransactions: MutableList<RewardTransactionMessage>
) : BlockMessage(height, previousHash, blockTimestamp, publicKey, hash, signature) {

    constructor(block: MainBlock) : this(block.height, block.previousHash, block.timestamp, block.hash,
        block.signature!!, block.publicKey, block.merkleHash,
        block.transactions.filterIsInstance(TransferTransaction::class.java).map { TransferTransactionMessage(it) }.toMutableList(),
        block.transactions.filterIsInstance(VoteTransaction::class.java).map { VoteTransactionMessage(it) }.toMutableList(),
        block.transactions.filterIsInstance(DelegateTransaction::class.java).map { DelegateTransactionMessage(it) }.toMutableList(),
        block.transactions.filterIsInstance(RewardTransaction::class.java).map { RewardTransactionMessage(it) }.toMutableList())

    override fun read(buffer: ByteBuf) {
        super.read(buffer)

        merkleHash = buffer.readString()
        transferTransactions = buffer.readList()
        voteTransactions = buffer.readList()
        delegateTransactions = buffer.readList()
        rewardTransactions = buffer.readList()
    }

    override fun write(buffer: ByteBuf) {
        super.write(buffer)

        buffer.writeString(merkleHash)
        buffer.writeList(transferTransactions)
        buffer.writeList(voteTransactions)
        buffer.writeList(delegateTransactions)
        buffer.writeList(rewardTransactions)
    }

    fun toEntity(): MainBlock = MainBlock(
        height,
        previousHash,
        blockTimestamp,
        publicKey,
        merkleHash,
        mutableSetOf()).apply { signature = super.signature }


    override fun toString() = "MainBlockMessage(hash=$hash)"

}
