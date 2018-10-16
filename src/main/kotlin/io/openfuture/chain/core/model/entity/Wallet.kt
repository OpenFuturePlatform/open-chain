package io.openfuture.chain.core.model.entity

import io.openfuture.chain.core.model.entity.base.BaseModel
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "wallets")
class Wallet(

    @Column(name = "address", nullable = false)
    var address: String,

    @Column(name = "balance", nullable = false)
    var balance: Long = 0

) : BaseModel()