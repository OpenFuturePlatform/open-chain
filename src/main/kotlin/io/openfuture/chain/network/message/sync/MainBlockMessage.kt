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
    merkleHash: String,
    stateHash: String,
    rewardTransaction: RewardTransactionMessage,
    voteTransactions: List<VoteTransactionMessage>,
    delegateTransactions: List<DelegateTransactionMessage>,
    transferTransactions: List<TransferTransactionMessage>
) : BaseMainBlockMessage(height, previousHash, timestamp, hash, signature, publicKey, merkleHash, stateHash,
    rewardTransaction, voteTransactions, delegateTransactions, transferTransactions) {

    override fun toString() = "MainBlockMessage(hash=$hash)"

}