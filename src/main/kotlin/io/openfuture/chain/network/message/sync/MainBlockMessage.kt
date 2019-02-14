package io.openfuture.chain.network.message.sync

import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.message.core.*

@NoArgConstructor
class MainBlockMessage(
    height: Long,
    previousHash: String,
    timestamp: Long,
    hash: String,
    signature: String,
    publicKey: String,
    transactionMerkleHash: String,
    stateMerkleHash: String,
    receiptMerkleHash: String,
    rewardTransactions: List<RewardTransactionMessage>,
    voteTransactions: List<VoteTransactionMessage>,
    delegateTransactions: List<DelegateTransactionMessage>,
    transferTransactions: List<TransferTransactionMessage>,
    delegateStates: List<DelegateStateMessage>,
    accountStates: List<AccountStateMessage>,
    receipts: List<ReceiptMessage>
) : BaseMainBlockMessage(height, previousHash, timestamp, hash, signature, publicKey, transactionMerkleHash,
    stateMerkleHash, receiptMerkleHash, rewardTransactions, voteTransactions, delegateTransactions, transferTransactions,
    delegateStates, accountStates, receipts) {

    override fun toString() = "MainBlockMessage(hash=$hash)"

}