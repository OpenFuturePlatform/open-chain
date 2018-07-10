package io.openfuture.chain.entity

import io.openfuture.chain.entity.base.BaseModel
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "peers")
class Peer (

    @Column(name = "network_id", nullable = false)
    var networkId : String,

    @Column(name = "host", nullable = false)
    var host: String,

    @Column(name = "port", nullable = false)
    var port: Int

) : BaseModel()