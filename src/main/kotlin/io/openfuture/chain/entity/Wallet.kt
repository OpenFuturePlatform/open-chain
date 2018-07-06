package io.openfuture.chain.entity

import io.openfuture.chain.entity.base.BaseModel
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "transactions")
class Wallet(

        @Column(name = "address", nullable = false)
        var address: String,

        @Column(name = "balance", nullable = false)
        var balance: Double = 0.0

) : BaseModel()