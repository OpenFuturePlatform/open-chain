package io.openfuture.chain.entity

import io.openfuture.chain.entity.base.BaseModel
import javax.persistence.*

@Entity
@Table(name = "blocks")
class Block(

    @Column(name = "height", nullable = false)
    var height: Int = 0,

    @Column(name = "nonce", nullable = false)
    var nonce: Long = 0,

    @Column(name = "timestamp", nullable = false)
    var timestamp: Long,

    @Column(name = "previous_hash", nullable = false)
    var previousHash: String,

    @Column(name = "hash", nullable = false)
    var hash: String,

    @Column(name = "merkle_hash", nullable = false)
    var merkleHash: String,

    @Column(name = "node_key", nullable = false)
    var nodeKey: String,

    @Column(name = "node_signature", nullable = false)
    var nodeSignature: String,

    @OneToMany(mappedBy = "block", fetch = FetchType.EAGER)
    var transactions: MutableList<Transaction> = mutableListOf()


) : BaseModel()


