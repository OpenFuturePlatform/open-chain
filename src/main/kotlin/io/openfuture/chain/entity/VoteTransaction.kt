package io.openfuture.chain.entity

import io.openfuture.chain.domain.transaction.TransactionDto
import io.openfuture.chain.domain.transaction.VoteTransactionDto
import io.openfuture.chain.entity.dictionary.TransactionType
import javax.persistence.*

@Entity
@Table(name = "vote_transactions")
class VoteTransaction(
        timestamp: Long,
        amount: Long,
        recipientKey: String,
        senderKey: String,
        senderSignature: String,
        hash: String,
        block: Block? = null,

        @OneToMany(mappedBy = "transaction", fetch = FetchType.EAGER)
        val votes: MutableList<Vote> = mutableListOf()

) : Transaction(TransactionType.VOTE.getId(), timestamp, amount, recipientKey, senderKey, senderSignature, hash,
        block) {

    override fun toDto(): VoteTransactionDto = VoteTransactionDto(
            this.timestamp,
            this.amount,
            this.recipientKey,
            this.senderKey,
            this.senderSignature,
            this.hash,
            this.votes.map { it.toDto() }.toMutableList()
    )

}