package io.openfuture.chain.core.model.entity

import io.openfuture.chain.core.model.entity.base.BaseModel
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "delegates")
class Delegate(

    @Column(name = "public_key", nullable = false, unique = true)
    var publicKey: String,

    @Column(name = "node_id", nullable = false, unique = true)
    var nodeId: String,

    @Column(name = "address", nullable = false)
    var address: String,

    @Column(name = "host", nullable = false)
    var host: String,

    @Column(name = "port", nullable = false)
    var port: Int,

    @Column(name = "registration_date", nullable = false)
    var registrationDate: Long,

    id: Long = 0L

) : BaseModel(id)