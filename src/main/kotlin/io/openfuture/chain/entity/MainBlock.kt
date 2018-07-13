package io.openfuture.chain.entity

import javax.persistence.Entity
import javax.persistence.JoinColumn
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "main_blocks")
class MainBlock(height: Long, previousHash: String,
        merkleHash: String, timestamp: Long, signature: String,

    @OneToMany
    @JoinColumn(name = "block_hash", nullable = true)
    var transactions: List<Transaction>

) : Block(height, previousHash, merkleHash, timestamp, signature, BlockType.MAIN.id)