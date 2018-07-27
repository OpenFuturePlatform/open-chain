package io.openfuture.chain.entity.block

import io.openfuture.chain.crypto.signature.SignatureManager
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.entity.Delegate
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import javax.persistence.*

@Entity
@Table(name = "genesis_blocks")
class GenesisBlock(
    privateKey: ByteArray,
    height: Long,
    previousHash: String,
    timestamp: Long,
    publicKey: ByteArray,

    @Column(name = "epoch_index", nullable = false)
    var epochIndex: Long,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "delegate2genesis",
        joinColumns = [JoinColumn(name = "genesis_id")],
        inverseJoinColumns = [(JoinColumn(name = "delegate_id"))])
    var activeDelegates: Set<Delegate>

) : Block(height, previousHash, timestamp, HashUtils.toHexString(publicKey),
    signature = SignatureManager.sign((previousHash + timestamp + height).toByteArray(), privateKey),

    hash = ByteUtils.toHexString(
        HashUtils.doubleSha256((previousHash + timestamp + height + publicKey).toByteArray()))
)