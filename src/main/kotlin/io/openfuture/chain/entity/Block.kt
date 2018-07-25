package io.openfuture.chain.entity

import io.openfuture.chain.crypto.signature.SignatureManager
import io.openfuture.chain.entity.base.BaseModel
import io.openfuture.chain.util.BlockUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import javax.persistence.*

@Entity
@Table(name = "blocks")
@Inheritance(strategy = InheritanceType.JOINED)
abstract class Block(

    privateKey: ByteArray,

    @Column(name = "height", nullable = false)
    var height: Long,

    @Column(name = "previous_hash", nullable = false)
    var previousHash: String,

    @Column(name = "merkle_hash", nullable = false)
    var merkleHash: String,

    @Column(name = "timestamp", nullable = false)
    var timestamp: Long,

    @Column(name = "public_key", nullable = false)
    val publicKey: String,

    @Column(name = "signature", nullable = false)
    var signature: String = SignatureManager.sign(
        (previousHash + merkleHash + timestamp + height).toByteArray(), privateKey),

    @Column(name = "hash", nullable = false)
    var hash: String = ByteUtils.toHexString(
        BlockUtils.calculateHash(
            previousHash,
            timestamp,
            height,
            merkleHash,
            publicKey)
    )

) : BaseModel()