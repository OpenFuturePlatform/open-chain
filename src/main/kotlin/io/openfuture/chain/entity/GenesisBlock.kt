package io.openfuture.chain.entity

import org.apache.commons.lang3.StringUtils
import javax.persistence.*

@Entity
@Table(name = "genesis_blocks")
class GenesisBlock(privateKey: ByteArray, height: Long,
        previousHash: String, timestamp: Long,

    @Column(name = "epoch_index", nullable = false)
    var epochIndex: Long,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="active_delegates", joinColumns=[JoinColumn(name = "genesis_block_id")])
    @Column(name="delegate_key")
    var activeDelegateKeys: Set<String>

) : Block(privateKey, height, previousHash, StringUtils.EMPTY, timestamp, BlockType.GENESIS.id)