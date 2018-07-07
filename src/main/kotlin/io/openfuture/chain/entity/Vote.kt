package io.openfuture.chain.entity

import io.openfuture.chain.domain.transaction.vote.VoteDto
import io.openfuture.chain.entity.base.BaseModel
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

) : BaseModel() {

    fun toDto(): VoteDto = VoteDto(
            this.publicKey,
            this.weight
    )

}

