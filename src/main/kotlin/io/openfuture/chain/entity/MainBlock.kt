package io.openfuture.chain.entity

import io.openfuture.chain.entity.transaction.BaseTransaction
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "main_blocks")
class MainBlock : Block {

    constructor(height: Long, previousHash: String, merkleHash: String, timestamp: Long, hash: String, signature: String,
                transactions: MutableList<BaseTransaction>)
        : super(height, previousHash, merkleHash, timestamp, BlockType.MAIN.id, hash, signature, transactions)

    constructor(privateKey: ByteArray, height: Long, previousHash: String, merkleHash: String, timestamp: Long,
                transactions: MutableList<BaseTransaction>)
        : super(privateKey, height, previousHash, merkleHash, timestamp, BlockType.MAIN.id, transactions)

}