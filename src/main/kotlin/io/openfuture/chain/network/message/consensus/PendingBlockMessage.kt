package io.openfuture.chain.network.message.consensus

import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.message.core.*

@NoArgConstructor
class PendingBlockMessage(
    height: Long,
    previousHash: String,
    timestamp: Long,
    hash: String,
    signature: String,
    publicKey: String,
    merkleHash: String,
    rewardTransaction: RewardTransactionMessage,
    voteTransactions: List<VoteTransactionMessage>,
    delegateTransactions: List<DelegateTransactionMessage>,
    transferTransactions: List<TransferTransactionMessage>
) : BaseMainBlockMessage(height, previousHash, timestamp, hash, signature, publicKey, merkleHash, rewardTransaction,
    voteTransactions, delegateTransactions, transferTransactions) {

    fun getExternalTransactions(): List<TransactionMessage> =
        voteTransactions + delegateTransactions + transferTransactions

    override fun toString() = "PendingBlockMessage(hash=$hash)"

}