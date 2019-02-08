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
    transactionMerkleHash: String,
    stateMerkleHash: String,
    receiptMerkleHash: String,
    rewardTransaction: RewardTransactionMessage,
    voteTransactions: List<VoteTransactionMessage>,
    delegateTransactions: List<DelegateTransactionMessage>,
    transferTransactions: List<TransferTransactionMessage>,
    delegateStates: List<DelegateStateMessage>,
    accountStates: List<AccountStateMessage>,
    receipts: List<ReceiptMessage>
) : BaseMainBlockMessage(height, previousHash, timestamp, hash, signature, publicKey, transactionMerkleHash,
    stateMerkleHash, receiptMerkleHash, rewardTransaction, voteTransactions, delegateTransactions, transferTransactions,
    delegateStates, accountStates, receipts) {

    fun getExternalTransactions(): List<TransactionMessage> =
        voteTransactions + delegateTransactions + transferTransactions

    override fun toString() = "PendingBlockMessage(hash=$hash)"

}