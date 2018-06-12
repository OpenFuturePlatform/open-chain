package io.openfuture.chain.entity

import io.openfuture.chain.entity.base.BaseModel
import javax.persistence.*

/**
 * @author Homza Pavel
 */
@Entity
@Table(name = "blocks")
class Block (

        @Column(name = "version", nullable = false)
        val version: Int = 0,

        @Column(name = "size", nullable = false)
        var size: Int = 0,

        @Column(name = "timestamp", nullable = false)
        val timestamp: Long,

        @Column(name = "merkle_hash", nullable = false)
        var merkleHash: String,

        @Column(name = "hash", nullable =  false)
        var hash: String,

        @Column(name = "previous_hash", nullable = false)
        val previousHash: String,

        @Column(name = "signature", nullable = false)
        val signature: String,

        @OneToMany(mappedBy = "block")
        val transactions: List<Transaction> = emptyList()

) : BaseModel()