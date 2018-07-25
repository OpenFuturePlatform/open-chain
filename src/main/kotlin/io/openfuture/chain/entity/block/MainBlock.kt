package io.openfuture.chain.entity.block

import io.openfuture.chain.entity.transaction.Transaction
import javax.persistence.Entity
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "main_blocks")
class MainBlock(
    privateKey: ByteArray,
    height: Long,
    previousHash: String,
    merkleHash: String,
    timestamp: Long,

    @OneToMany(mappedBy = "block")
    var transactions: MutableList<Transaction>

) : Block(privateKey, height, previousHash, merkleHash, timestamp, BlockType.MAIN.id)