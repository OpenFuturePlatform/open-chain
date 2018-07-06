package io.openfuture.chain.entity

import io.openfuture.chain.entity.base.BaseModel
import javax.persistence.*

@Entity
@Table(name = "blocks")
class Block(

    @Column(name = "hash", nullable = false)
    var hash: String,

    @Column(name = "height", nullable = false)
    var height: Long,

    @Column(name = "signature", nullable = false)
    var signature: String,

    @Column(name = "previous_hash", nullable = false)
    var previousHash: String,

    @Column(name = "merkle_hash", nullable = false)
    var merkleHash: String,

    @Column(name = "timestamp", nullable = false)
    var timestamp: Long,

    @OneToMany
    @JoinColumn(name = "block_id", nullable = true)
    var transactions: Set<Transaction>

) : BaseModel()


