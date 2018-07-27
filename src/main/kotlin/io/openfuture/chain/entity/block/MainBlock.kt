package io.openfuture.chain.entity

import io.openfuture.chain.crypto.signature.SignatureManager
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.entity.transaction.BaseTransaction
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import javax.persistence.*

@Entity
@Table(name = "main_blocks")
class MainBlock(height: Long, previousHash: String, timestamp: Long, publicKey: String,

                @Column(name = "merkle_hash", nullable = false)
                var merkleHash: String,

                @OneToMany(mappedBy = "block", fetch = FetchType.EAGER)
                var transactions: MutableSet<BaseTransaction>

) : Block(height, previousHash, timestamp, publicKey,
    ByteUtils.toHexString(HashUtils.doubleSha256((previousHash + merkleHash + timestamp + height + publicKey).toByteArray()))) {

    @Suppress("UNCHECKED_CAST")
    fun <T : Block> sign(privateKey: ByteArray): T {
        this.signature = SignatureManager.sign(
            StringBuilder().append(previousHash).append(merkleHash).append(timestamp).append(height).toString()
                .toByteArray(), privateKey)
        return this as T
    }

}