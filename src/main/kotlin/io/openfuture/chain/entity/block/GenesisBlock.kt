package io.openfuture.chain.entity.block

import io.openfuture.chain.entity.dictionary.BlockVersion
import javax.persistence.*

@Entity
@Table(name = "genesis_blocks")
class GenesisBlock(hash: String, height: Long,
        previousHash: String, merkleHash: String, timestamp: Long,

    @Column(name = "epoch_index", nullable = false)
    var epochIndex: Long,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="active_delegates", joinColumns=[JoinColumn(name = "genesis_block_id")])
    @Column(name="delegate_ip")
    var activeDelegateIps: Set<String>

) : Block(hash, height, previousHash, merkleHash, timestamp, BlockVersion.GENESIS.version)