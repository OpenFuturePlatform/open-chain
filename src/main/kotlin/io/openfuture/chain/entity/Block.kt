package io.openfuture.chain.entity

import io.openfuture.chain.crypto.signature.SignatureManager
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.entity.base.BaseModel
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import javax.persistence.*

@Entity
@Table(name = "blocks")
@Inheritance(strategy = InheritanceType.JOINED)
abstract class Block(

    @Column(name = "height", nullable = false)
    var height: Long,

    @Column(name = "previous_hash", nullable = false)
    var previousHash: String,

    @Column(name = "merkle_hash", nullable = false)
    var merkleHash: String,

    @Column(name = "timestamp", nullable = false)
    var timestamp: Long,

    @Column(name = "typeId", nullable = false)
    var typeId: Int,

    @Column(name = "hash", nullable = false)
    var hash: String = ByteUtils.toHexString(HashUtils.doubleSha256((previousHash + merkleHash + timestamp + height).toByteArray())),

    @Column(name = "signature", nullable = false)
    var signature: String = SignatureManager.sign(hash.toByteArray(), privateKey)

) : BaseModel() {

    @Suppress("UNCHECKED_CAST")
    fun <T : Block> sign(privateKey: ByteArray): T {
        this.signature = SignatureManager.sign(HashUtils.fromHexString(hash), privateKey)
        return this as T
    }

}

    fun getPublicKey(): String{
//        TODO("Need to add public key")
        return "publicKey"
    }

}