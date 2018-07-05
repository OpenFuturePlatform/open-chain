package io.openfuture.chain.entity

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

)


