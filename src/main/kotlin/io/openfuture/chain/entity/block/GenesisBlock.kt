package io.openfuture.chain.entity.block

import io.openfuture.chain.crypto.signature.SignatureManager
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.entity.Delegate
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import javax.persistence.*

@Entity
@Table(name = "genesis_blocks")
class GenesisBlock(
    height: Long,
    previousHash: String,
    timestamp: Long,
    publicKey: String,

    @Column(name = "epoch_index", nullable = false)
    var epochIndex: Long,

    @ManyToMany(fetch = FetchType.EAGER, cascade = [(CascadeType.ALL)])
    @JoinTable(name = "delegate2genesis",
        joinColumns = [JoinColumn(name = "genesis_id")],
        inverseJoinColumns = [(JoinColumn(name = "delegate_id"))])
    var activeDelegates: Set<Delegate>

) : Block(height, previousHash, timestamp, publicKey,
    ByteUtils.toHexString(HashUtils.doubleSha256((previousHash + timestamp + height + publicKey).toByteArray()))
) {

    @Suppress("UNCHECKED_CAST")
    fun <T : Block> sign(privateKey: ByteArray): T {
        this.signature = SignatureManager.sign((previousHash + timestamp + height).toByteArray(), privateKey)
        return this as T
    }

}