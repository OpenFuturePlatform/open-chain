package io.openfuture.chain.entity

import javax.persistence.*

@Entity
@Table(name = "main_blocks")
class MainBlock(hash: String, height: Long, previousHash: String,
        merkleHash: String, timestamp: Long,

    @Column(name = "signature", nullable = false)
    var signature: String,

    @OneToMany
    @JoinColumn(name = "block_id", nullable = true)
    var transactions: List<Transaction>

) : Block(hash, height, previousHash, merkleHash, timestamp, BlockVersion.MAIN.version)