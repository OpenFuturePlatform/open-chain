package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.RewardTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import io.openfuture.chain.network.extension.readList
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeList
import io.openfuture.chain.network.extension.writeString

@NoArgConstructor
class MainBlockMessage(
    height: Long,
    previousHash: String,
    timestamp: Long,
    hash: String,
    signature: String,
    publicKey: String,
    var merkleHash: String,
    var rewardTransaction: RewardTransactionMessage,
    var voteTransactions: List<VoteTransactionMessage>,
    var delegateTransactions: List<DelegateTransactionMessage>,
    var transferTransactions: List<TransferTransactionMessage>
) : BlockMessage(height, previousHash, timestamp, hash, signature, publicKey) {

    constructor(block: MainBlock, rewardTransaction: RewardTransaction, voteTransactions: List<VoteTransaction>,
                delegateTransactions: List<DelegateTransaction>, transferTransactions: List<TransferTransaction>) : this(
        block.height,
        block.previousHash,
        block.timestamp,
        block.hash,
        block.signature,
        block.publicKey,
        block.payload.merkleHash,
        RewardTransactionMessage(rewardTransaction),
        voteTransactions.map { VoteTransactionMessage(it) },
        delegateTransactions.map { DelegateTransactionMessage(it) },
        transferTransactions.map { TransferTransactionMessage(it) }
    )

    fun getAllTransactions(): List<BaseTransactionMessage> {
        return listOf(rewardTransaction) + voteTransactions + delegateTransactions + transferTransactions
    }

    override fun read(buffer: ByteBuf) {
        super.read(buffer)

        merkleHash = buffer.readString()
        rewardTransaction = RewardTransactionMessage::class.java.newInstance()
        rewardTransaction.read(buffer)
        voteTransactions = buffer.readList()
        delegateTransactions = buffer.readList()
        transferTransactions = buffer.readList()
    }

    override fun write(buffer: ByteBuf) {
        super.write(buffer)

        buffer.writeString(merkleHash)
        rewardTransaction.write(buffer)
        buffer.writeList(voteTransactions)
        buffer.writeList(delegateTransactions)
        buffer.writeList(transferTransactions)
    }

}
