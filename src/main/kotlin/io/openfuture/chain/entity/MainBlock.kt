package io.openfuture.chain.entity

import io.openfuture.chain.entity.transaction.BaseTransaction
import javax.persistence.Entity
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "main_blocks")
class MainBlock(height: Long, previousHash: String,
        merkleHash: String, timestamp: Long, signature: String,

    @OneToMany(mappedBy = "block")
    var transactions: MutableList<BaseTransaction>

) : Block(height, previousHash, merkleHash, timestamp, signature, BlockType.MAIN.id)