package io.openfuture.chain.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "nodes")
class Node(

    @Id
    @Column(name = "public_key")
    var publicKey : ByteArray,

    @Column(name = "host", nullable = false)
    var host: String,

    @Column(name = "port", nullable = false)
    var port: Int

)