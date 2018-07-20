package io.openfuture.chain.domain.transaction

import io.openfuture.chain.entity.dictionary.VoteType
import io.openfuture.chain.entity.transaction.VoteTransaction

class VoteTransactionDto(timestamp: Long, amount: Double, recipientAddress: String, senderKey: String,
                         senderAddress: String, senderSignature: String, hash: String,
    val voteType: VoteType,
    val delegateKey: String
) : BaseTransactionDto(timestamp, amount, recipientAddress, senderKey, senderAddress, senderSignature,
    hash) {

    constructor(tx: VoteTransaction) : this(
        tx.timestamp,
        tx.amount,
        tx.recipientAddress,
        tx.senderPublicKey,
        tx.senderAddress,
        tx.senderSignature,
        tx.hash,
        tx.getVoteType(),
        tx.delegateKey
    )

}