package io.openfuture.chain.entity

import io.openfuture.chain.domain.transaction.vote.VoteDto
import javax.persistence.*

@Entity
@Table(name = "votes")
class Vote(

        @ManyToOne
        @JoinColumn(name = "transaction_id", nullable = false)
        var transaction: VoteTransaction,

        @Column(name = "public_key", nullable = false)
        var publicKey: String,

        @Column(name = "weight", nullable = false)
        var weight: Int

) {

    companion object {
        fun of(transaction: VoteTransaction, dto: VoteDto): Vote = Vote(
                transaction,
                dto.pubicKey,
                dto.weight
        )
    }

}

