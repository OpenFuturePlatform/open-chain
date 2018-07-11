package io.openfuture.chain.entity.account

import io.openfuture.chain.domain.stakeholder.DelegateDto
import javax.persistence.*

@Entity
@Table(name = "delegates")
class Delegate(
    username: String,
    address: String,
    publicKey: String,

    @Column(name = "rating", nullable = false)
    var rating: Int = 0

) : Stakeholder(username, address, publicKey) {

    companion object {
        fun of(delegateDto: DelegateDto): Delegate = Delegate(
            delegateDto.username,
            delegateDto.address,
            delegateDto.publicKey,
            delegateDto.rating
        )
    }

}