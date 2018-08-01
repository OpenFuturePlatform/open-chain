package io.openfuture.chain.core.model.entity.block

import io.openfuture.chain.core.model.entity.transaction.Transaction
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import javax.persistence.*

@Entity
@Table(name = "main_blocks")
class MainBlock(
    height: Long,
    previousHash: String,
    timestamp: Long,
    reward: Long,
    publicKey: String,

    @Column(name = "merkle_hash", nullable = false)
    var merkleHash: String,

    @OneToMany(mappedBy = "block", fetch = FetchType.EAGER)
    var transactions: MutableSet<Transaction>

) : BaseBlock(height, previousHash, timestamp, reward, publicKey,
    ByteUtils.toHexString(HashUtils.doubleSha256((previousHash + merkleHash + timestamp + height + publicKey).toByteArray()))) {

    override fun sign(privateKey: ByteArray): MainBlock {
        this.signature = SignatureUtils.sign((previousHash + merkleHash + timestamp + height).toByteArray(), privateKey)
        return this
    }

}