package io.openfuture.chain.entity

import io.openfuture.chain.domain.block.BlockRequest
import io.openfuture.chain.entity.base.BaseModel
import javax.persistence.*

@Entity
@Table(name = "blocks")
class Block(

        @Column(name = "order_number", nullable = false)
        val orderNumber: Int = 0,

        @Column(name = "nonce", nullable = false)
        val nonce: Long = 0,

        @Column(name = "timestamp", nullable = false)
        val timestamp: Long,

        @Column(name = "previous_hash", nullable = false)
        val previousHash: String,

        @Column(name = "hash", nullable = false)
        val hash: String,

        @Column(name = "merkle_hash", nullable = false)
        val merkleHash: String,

        @Column(name = "node_key", nullable = false)
        val nodeKey: String,

        @Column(name = "node_signature", nullable = false)
        val nodeSignature: String,

        @OneToMany(mappedBy = "block", fetch = FetchType.EAGER)
        val transactions: MutableList<Transaction> = mutableListOf()


) : BaseModel() {
    companion object {
        fun of(request: BlockRequest): Block = Block(
            request.orderNumber,
            request.nonce,
            request.timestamp,
            request.previousHash,
            request.hash,
            request.merkleHash,
            request.nodeKey,
            request.nodeSignature
        )
    }
}


