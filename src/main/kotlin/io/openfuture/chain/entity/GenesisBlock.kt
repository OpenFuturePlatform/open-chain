package io.openfuture.chain.entity

import org.apache.commons.lang3.StringUtils
import javax.persistence.*

@Entity
@Table(name = "genesis_blocks")
class GenesisBlock(height: Long, previousHash: String, timestamp: Long,

                   @Column(name = "epoch_index", nullable = false)
                   var epochIndex: Long,

                   @ManyToMany
                   @JoinTable(name = "delegate2genesis",
                       joinColumns = [JoinColumn(name = "genesis_id")],
                       inverseJoinColumns = [(JoinColumn(name = "delegate_id"))])
                   var activeDelegates: Set<Delegate>

) : Block(height, previousHash, StringUtils.EMPTY, timestamp, BlockType.GENESIS.id)