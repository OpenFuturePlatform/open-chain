package io.openfuture.chain.entity

import io.openfuture.chain.entity.transaction.BaseTransaction
import javax.persistence.*

@Entity
@Table(name = "genesis_blocks")
class GenesisBlock : Block {

    @Column(name = "epoch_index", nullable = false)
    var epochIndex: Long = 0

    @ManyToMany
    @JoinTable(name = "delegate2genesis",
        joinColumns = [JoinColumn(name = "genesis_id")],
        inverseJoinColumns = [(JoinColumn(name = "delegate_id"))])
    var activeDelegates: Set<Delegate> = setOf()


    constructor(height: Long, previousHash: String, merkleHash: String, timestamp: Long, hash: String, signature: String,
                transactions: MutableList<BaseTransaction>, epochIndex: Long, activeDelegates: Set<Delegate>)
        : super(height, previousHash, merkleHash, timestamp, BlockType.GENESIS.id, hash, signature, transactions) {
        this.epochIndex = epochIndex
        this.activeDelegates = activeDelegates
    }

    constructor(privateKey: ByteArray, height: Long, previousHash: String, merkleHash: String, timestamp: Long,
                transactions: MutableList<BaseTransaction>, epochIndex: Long, activeDelegates: Set<Delegate>)
        : super(privateKey, height, previousHash, merkleHash, timestamp, BlockType.GENESIS.id, transactions) {
        this.epochIndex = epochIndex
        this.activeDelegates = activeDelegates
    }

}