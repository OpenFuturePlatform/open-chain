package io.openfuture.chain.entity.block

import io.openfuture.chain.entity.base.BaseModel
import javax.persistence.*

@Entity
@Table(name = "blocks")
@Inheritance(strategy = InheritanceType.JOINED)
abstract class Block(

    @Column(name = "height", nullable = false)
    var height: Long,

    @Column(name = "previous_hash", nullable = false)
    var previousHash: String,

    @Column(name = "timestamp", nullable = false)
    var timestamp: Long,

    @Column(name = "public_key", nullable = false)
    val publicKey: String,

    @Column(name = "hash", nullable = false, unique = true)
    var hash: String

    @Column(name = "signature", nullable = false)
    var signature: String? = null

) : BaseModel()
