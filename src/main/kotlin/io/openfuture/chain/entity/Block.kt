package io.openfuture.chain.entity

import io.openfuture.chain.crypto.signature.SignatureManager
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.entity.base.BaseModel
import io.openfuture.chain.entity.transaction.BaseTransaction
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import javax.persistence.*

@Entity
@Table(name = "blocks")
@Inheritance(strategy = InheritanceType.JOINED)
abstract class Block : BaseModel {

    @Column(name = "height", nullable = false)
    var height: Long = 0

    @Column(name = "previous_hash", nullable = false)
    var previousHash: String = ""

    @Column(name = "merkle_hash", nullable = false)
    var merkleHash: String = ""

    @Column(name = "timestamp", nullable = false)
    var timestamp: Long = 0

    @Column(name = "typeId", nullable = false)
    var typeId: Int = 0

    @Column(name = "hash", nullable = false)
    var hash: String = ""

    @Column(name = "signature", nullable = false)
    var signature: String = ""

    @OneToMany(mappedBy = "block")
    var transactions: MutableList<BaseTransaction> = mutableListOf()


    constructor(height: Long, previousHash: String, merkleHash: String, timestamp: Long, typeId: Int, hash: String,
                signature: String, transactions: MutableList<BaseTransaction>) {
        this.height = height
        this.previousHash = previousHash
        this.merkleHash = merkleHash
        this.timestamp = timestamp
        this.typeId = typeId
        this.hash = hash
        this.signature = signature
        this.transactions = transactions
    }

    constructor(privateKey: ByteArray, height: Long, previousHash: String, merkleHash: String, timestamp: Long,
                typeId: Int, transactions: MutableList<BaseTransaction>)
        : this(height, previousHash, merkleHash, timestamp, typeId,
        calculateHash(previousHash, merkleHash, timestamp, height),
        SignatureManager.sign(calculateHash(previousHash, merkleHash, timestamp, height).toByteArray(), privateKey),
        transactions
    )

    companion object {
        private fun calculateHash(previousHash: String, merkleHash: String, timestamp: Long, height: Long): String =
            ByteUtils.toHexString(HashUtils.doubleSha256((previousHash + merkleHash + timestamp + height).toByteArray()))
    }

}


