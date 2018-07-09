package io.openfuture.chain.domain.transaction

import io.openfuture.chain.entity.transaction.VoteTransaction
import io.openfuture.chain.entity.dictionary.VoteType

class VoteTransactionDto(
        timestamp: Long,
        amount: Long,
        recipientKey: String,
        senderKey: String,
        senderSignature: String,
        hash: String,
        val voteType: VoteType,
        val delegateKey: String,
        val weight: Int

) : TransactionDto(timestamp, amount, recipientKey, senderKey, senderSignature, hash) {

    override fun toEntity(): VoteTransaction = VoteTransaction(
            this.timestamp,
            this.amount,
            this.recipientKey,
            this.senderKey,
            this.senderSignature,
            this.hash,
            this.voteType.getId(),
            this.delegateKey,
            this.weight
    )

}