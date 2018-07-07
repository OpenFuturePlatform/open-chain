package io.openfuture.chain.entity

import io.openfuture.chain.domain.transaction.VoteTransactionDto
import io.openfuture.chain.entity.dictionary.TransactionType
import javax.persistence.*

@Entity
@Table(name = "vote_transaction")
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

    companion object {
        fun of(dto: VoteTransactionDto): VoteTransaction = VoteTransaction(
                dto.timestamp,
                dto.amount,
                dto.recipientKey,
                dto.senderKey,
                dto.senderSignature,
                dto.hash
        )
    }
}