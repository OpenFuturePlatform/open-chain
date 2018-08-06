package io.openfuture.chain.network.message.consensus

import io.openfuture.chain.network.annotation.NoArgConstructor
import io.openfuture.chain.network.message.core.MainBlockMessage

@NoArgConstructor
class PendingBlockMessage(
    height: Long,
    previousHash: String,
    blockTimestamp: Long,
    reward: Long,
    hash: String,
    signature: String,
    publicKey: String,
    merkleHash: String,
    transactions: List<String>
) : MainBlockMessage(height, previousHash, blockTimestamp, reward, publicKey, hash, signature, merkleHash, transactions) {

    override fun toString() = "PendingBlockMessage(hash=$hash)"

}
