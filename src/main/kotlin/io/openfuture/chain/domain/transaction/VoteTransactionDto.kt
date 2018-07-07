package io.openfuture.chain.domain.transaction

import io.openfuture.chain.domain.transaction.vote.VoteDto
import io.openfuture.chain.entity.VoteTransaction
import io.openfuture.chain.entity.dictionary.TransactionType

class VoteTransactionDto(
        timestamp: Long,
        amount: Long,
        recipientKey: String,
        senderKey: String,
        senderSignature: String,
        hash: String,
        val votes: List<VoteDto>

) : TransactionDto(timestamp, amount, recipientKey, senderKey, senderSignature, hash) {

    constructor(transaction: VoteTransaction): this(
            transaction.timestamp,
            transaction.amount,
            transaction.recipientKey,
            transaction.senderKey,
            transaction.senderSignature,
            transaction.hash,
            transaction.votes.map { VoteDto.of(it) }
    )

}