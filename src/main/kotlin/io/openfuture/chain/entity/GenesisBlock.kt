package io.openfuture.chain.entity

import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.util.BlockUtils
import javax.persistence.*

@Entity
@Table(name = "genesis_blocks")
class GenesisBlock(privateKey: ByteArray, height: Long,
        previousHash: String, timestamp: Long, publicKey: ByteArray,

    @Column(name = "epoch_index", nullable = false)
    var epochIndex: Long,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "delegate2genesis",
        joinColumns = [JoinColumn(name = "genesis_id")],
        inverseJoinColumns = [(JoinColumn(name = "delegate_id"))])
    var activeDelegates: Set<Delegate>

) : Block(privateKey, height, previousHash, BlockUtils.calculateDelegatesHash(activeDelegates), timestamp,
    BlockType.GENESIS.id, HashUtils.toHexString(publicKey))