package io.openfuture.chain.entity

import io.openfuture.chain.crypto.signature.SignatureManager
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.entity.transaction.BaseTransaction
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.OneToMany
import javax.persistence.Table

@Entity
@Table(name = "main_blocks")
class MainBlock(privateKey: ByteArray, height: Long, previousHash: String,
        timestamp: Long, publicKey: ByteArray,

    @Column(name = "merkle_hash", nullable = false)
    var merkleHash: String,

    @OneToMany(mappedBy = "block", fetch = FetchType.EAGER))
    var transactions: MutableSet<BaseTransaction>

) : Block(height, previousHash, timestamp, HashUtils.toHexString(publicKey),
    signature = SignatureManager.sign(
        StringBuilder()
            .append(previousHash)
            .append(merkleHash)
            .append(timestamp)
            .append(height)
            .toString()
            .toByteArray(), privateKey),

    hash = ByteUtils.toHexString(
        HashUtils.doubleSha256((previousHash + merkleHash + timestamp + height + publicKey).toByteArray()))
)