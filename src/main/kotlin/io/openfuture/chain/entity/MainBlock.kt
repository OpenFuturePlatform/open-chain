package io.openfuture.chain.entity

import io.openfuture.chain.entity.transaction.BaseTransaction
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "main_blocks")
class MainBlock(height: Long, previousHash: String,
                merkleHash: String, timestamp: Long,

                @OneToMany(mappedBy = "block", fetch = FetchType.EAGER)
                var transactions: MutableList<BaseTransaction>

) : Block(height, previousHash, merkleHash, timestamp, BlockType.MAIN.id, merkleHash)