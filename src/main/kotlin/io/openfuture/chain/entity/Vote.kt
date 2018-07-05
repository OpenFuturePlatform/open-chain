package io.openfuture.chain.entity

import io.openfuture.chain.domain.transaction.vote.VoteDto
import io.openfuture.chain.entity.base.BaseModel
import javax.persistence.*

@Entity
@Table(name = "votes")
class Vote(

        @ManyToOne
        @JoinColumn(name = "transaction_id")
        var transaction: Transaction,

        @Column(name = "public_key")
        var publicKey: String,

        @Column(name = "weight")
        var wieght: Int

) : BaseModel() {

    companion object {
        fun of(transaction: Transaction, dto: VoteDto): Vote = Vote(
                transaction,
                dto.pubicKey,
                dto.weight
        )
    }

}


