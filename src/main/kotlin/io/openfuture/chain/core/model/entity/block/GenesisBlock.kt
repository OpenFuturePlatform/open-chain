package io.openfuture.chain.core.model.entity.block

import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.network.domain.NetworkGenesisBlock
import javax.persistence.*

@Entity
@Table(name = "genesis_blocks")
class GenesisBlock(
    height: Long,
    previousHash: String,
    timestamp: Long,
    reward: Long,
    hash: String,
    publicKey: String,
    signature: String,

    @Column(name = "epoch_index", nullable = false)
    var epochIndex: Long,

    @ManyToMany(fetch = FetchType.EAGER, cascade = [(CascadeType.ALL)])
    @JoinTable(name = "delegate2genesis",
        joinColumns = [JoinColumn(name = "genesis_id")],
        inverseJoinColumns = [(JoinColumn(name = "delegate_id"))])
    var activeDelegates: Set<Delegate> = mutableSetOf()

) : BaseBlock(height, previousHash, timestamp, reward, hash, publicKey, signature) {

    companion object {
        fun of(dto: NetworkGenesisBlock) : GenesisBlock = GenesisBlock(
            dto.height,
            dto.previousHash,
            dto.timestamp,
            dto.reward,
            dto.hash!!,
            dto.publicKey!!,
            dto.signature!!,
            dto.epochIndex
        )
    }

    override fun toMessage(): NetworkGenesisBlock  = NetworkGenesisBlock (
        height,
        previousHash,
        timestamp,
        reward,
        hash,
        publicKey,
        signature,
        epochIndex,
        activeDelegates.map { it.toMessage() }.toMutableSet()
    )

}