package io.openfuture.chain.domain.block.nested

data class BlockData(
        val timestamp: Long,
        val orderNumber: Long,
        val previousHash: String,
        val merkleHash: MerkleHash
)