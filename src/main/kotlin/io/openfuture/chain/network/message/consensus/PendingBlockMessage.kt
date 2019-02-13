package io.openfuture.chain.network.message.consensus

import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.core.model.entity.block.MainBlock
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

    constructor(block: MainBlock) : this(
        block.height,
        block.previousHash,
        block.timestamp,
        block.hash,
        block.signature,
        block.publicKey,
        block.getPayload().transactionMerkleHash,
        block.getPayload().stateMerkleHash,
        block.getPayload().receiptMerkleHash,
        block.getPayload().rewardTransactions.map { it.toMessage() },
        block.getPayload().voteTransactions.map { it.toMessage() },
        block.getPayload().delegateTransactions.map { it.toMessage() },
        block.getPayload().transferTransactions.map { it.toMessage() },
        block.getPayload().delegateStates.map { it.toMessage() },
        block.getPayload().accountStates.map { it.toMessage() },
        block.getPayload().receipts.map { it.toMessage() }
    )

    override fun toString() = "PendingBlockMessage(hash=$hash)"

}