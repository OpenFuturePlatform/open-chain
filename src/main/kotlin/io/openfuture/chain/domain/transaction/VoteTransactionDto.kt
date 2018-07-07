package io.openfuture.chain.domain.transaction

import io.openfuture.chain.domain.transaction.vote.VoteDto
import io.openfuture.chain.entity.Transaction
import io.openfuture.chain.entity.Vote
import io.openfuture.chain.entity.VoteTransaction
import io.openfuture.chain.entity.dictionary.TransactionType

class VoteTransactionDto(
        timestamp: Long,
        amount: Long,
        recipientKey: String,
        senderKey: String,
        senderSignature: String,
        hash: String,
        val votes: MutableList<VoteDto>

) : TransactionDto(timestamp, amount, recipientKey, senderKey, senderSignature, hash) {

    override fun toEntity(): VoteTransaction = VoteTransaction(
            this.timestamp,
            this.amount,
            this.recipientKey,
            this.senderKey,
            this.senderSignature,
            this.hash
    )

}