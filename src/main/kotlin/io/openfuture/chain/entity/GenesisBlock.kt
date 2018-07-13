package io.openfuture.chain.entity

import org.apache.commons.lang3.StringUtils
import javax.persistence.*

@Entity
@Table(name = "genesis_blocks")
class GenesisBlock(height: Long,
        previousHash: String, timestamp: Long, signature: String,

    @Column(name = "epoch_index", nullable = false)
    var epochIndex: Long,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="active_delegates", joinColumns=[JoinColumn(name = "genesis_block_id")])
    @Column(name="delegate_key")
    var activeDelegateKeys: Set<String>

) : Block(height, previousHash, StringUtils.EMPTY, timestamp, signature, BlockType.GENESIS.id)