package io.openfuture.chain.entity

import io.openfuture.chain.entity.base.BaseModel
import javax.persistence.*

@Entity
@Table(name = "blocks")
class Block(
    @Column(name = "hash", nullable = false)
    private val hash: String,

    @Column(name = "height", nullable = false)
    private val height: Long,

    @Column(name = "signature", nullable = false)
    private val signature: String,

    @Column(name = "previous_hash", nullable = false)
    private val previousHash: String,

    @Column(name = "merkle_hash", nullable = false)
    val merkleHash: String,

    @Column(name = "timestamp", nullable = false)
    private val timestamp: Long,

    @OneToMany
    @JoinColumn(name = "block_id", nullable = true)
    val transactions: Set<Transaction>
) : BaseModel()


