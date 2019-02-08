package io.openfuture.chain.core.model.entity

import io.openfuture.chain.core.model.entity.base.BaseModel
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "contracts")
class Contract(

    @Column(name = "address", nullable = false)
    var address: String,

    @Column(name = "owner", nullable = false)
    var owner: String,

    @Column(name = "bytecode", nullable = false)
    var bytecode: String,

    @Column(name = "abi", nullable = false)
    var abi: String,

    @Column(name = "cost", nullable = false)
    var cost: Long

) : BaseModel()