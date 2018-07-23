package io.openfuture.chain.entity

import io.openfuture.chain.entity.base.BaseModel
import javax.persistence.*

@Embeddable

@Entity
@Table(name = "delegates")
class Delegate(

    @Column(name = "public_key", nullable = false, unique = true)
    var publicKey: String,

    @Column(name = "address", nullable = false)
    var address: String,

    id: Int = 0

) : BaseModel(id)