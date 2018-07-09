package io.openfuture.chain.entity

import io.openfuture.chain.domain.delegate.DelegateDto
import io.openfuture.chain.entity.base.BaseModel
import javax.persistence.*

@Entity
@Table(name = "delegates")
class Delegate(

        @Column(name = "username", nullable = false)
        val username: String,

        @Column(name = "address", nullable = false)
        val address: String,

        @Column(name = "public_key", nullable = false, unique = true)
        val publicKey: String,

        @Column(name = "rating", nullable = false)
        var rating: Int = 0,

        @ManyToMany(fetch = FetchType.EAGER)
        @JoinTable(
                name = "delegates_votes",
                joinColumns = [(JoinColumn(name = "vote_sender_id", nullable = false))],
                inverseJoinColumns = [(JoinColumn(name = "vote_recipient_id", nullable = false))]
        )
        val votes: MutableSet<Delegate> = mutableSetOf<Delegate>()


) : BaseModel() {

    companion object {
        fun of(delegateDto: DelegateDto): Delegate = Delegate(
                delegateDto.username,
                delegateDto.address,
                delegateDto.publicKey
        )
    }

}