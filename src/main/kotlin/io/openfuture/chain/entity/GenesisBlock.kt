package io.openfuture.chain.entity

import javax.persistence.*

@Entity
@Table(name = "genesis_blocks")
class GenesisBlock(hash: String, height: Long,
        previousHash: String, merkleHash: String, timestamp: Long,

    @Column(name = "epoch_index", nullable = false)
    var epochIndex: Long,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="active_delegates", joinColumns=[JoinColumn(name = "genesis_block_key")])
    @Column(name="delegate_key")
    var activeDelegateKeys: Set<String>

) : Block(hash, height, previousHash, merkleHash, timestamp, BlockVersion.GENESIS.version)