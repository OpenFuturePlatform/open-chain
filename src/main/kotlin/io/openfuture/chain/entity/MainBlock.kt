package io.openfuture.chain.entity

import io.openfuture.chain.entity.transaction.BaseTransaction
import javax.persistence.Entity
import javax.persistence.Table
import javax.persistence.Transient

@Entity
@Table(name = "main_blocks")
class MainBlock(height: Long, previousHash: String,
        merkleHash: String, timestamp: Long, signature: String,

    @Transient
    var transactions: List<BaseTransaction>

) : Block(height, previousHash, merkleHash, timestamp, signature, BlockType.MAIN.id)