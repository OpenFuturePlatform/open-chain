package io.openfuture.chain.network.message.consensus

import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.message.core.*
import io.openfuture.chain.network.message.sync.MainBlockMessage

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

    constructor(mainBlockMessage: MainBlockMessage) : this(
        mainBlockMessage.height,
        mainBlockMessage.previousHash,
        mainBlockMessage.timestamp,
        mainBlockMessage.hash,
        mainBlockMessage.signature,
        mainBlockMessage.publicKey,
        mainBlockMessage.transactionMerkleHash,
        mainBlockMessage.stateMerkleHash,
        mainBlockMessage.receiptMerkleHash,
        mainBlockMessage.rewardTransaction,
        mainBlockMessage.voteTransactions,
        mainBlockMessage.delegateTransactions,
        mainBlockMessage.transferTransactions,
        mainBlockMessage.delegateStates,
        mainBlockMessage.accountStates,
        mainBlockMessage.receipts
    )

    override fun toString() = "PendingBlockMessage(hash=$hash)"

}