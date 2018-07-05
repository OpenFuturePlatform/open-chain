package io.openfuture.chain.entity

import io.openfuture.chain.domain.transaction.VoteTransactionDto
import io.openfuture.chain.entity.dictionary.TransactionType
import javax.persistence.*

@Entity
@Table(name = "vote_transactions")
class VoteTransaction(
        block: Block?,
        timestamp: Long,
        hash: String,

        @OneToMany(mappedBy = "transaction", fetch = FetchType.EAGER)
        var votes: MutableList<Vote> = mutableListOf()

) : Transaction(timestamp, hash, TransactionType.VOTE.getId(), block) {

    companion object {
        fun of(dto: VoteTransactionDto): VoteTransaction = VoteTransaction(
                null,
                dto.timestamp,
                dto.hash
        )
    }

}