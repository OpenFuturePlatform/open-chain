package io.openfuture.chain.entity

import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.entity.transaction.BaseTransaction
import javax.persistence.Entity
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "main_blocks")
class MainBlock(privateKey: ByteArray, height: Long, previousHash: String,
        merkleHash: String, timestamp: Long, publicKey: ByteArray,

    @OneToMany(mappedBy = "block")
    var transactions: MutableSet<BaseTransaction>

) : Block(privateKey, height, previousHash, merkleHash, timestamp, BlockType.MAIN.id, HashUtils.toHexString(publicKey))