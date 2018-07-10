package io.openfuture.chain.entity.account

import javax.persistence.*

@Entity
@Table(name = "delegates")
class Delegate(
        username: String,
        address: String,
        publicKey: String,

        @Column(name = "rating", nullable = false)
        var rating: Int = 0

) : Stakeholder(username, address, publicKey)