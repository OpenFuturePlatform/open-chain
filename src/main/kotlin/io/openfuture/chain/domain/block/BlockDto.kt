package io.openfuture.chain.domain.block

import io.openfuture.chain.domain.block.nested.BlockData
import io.openfuture.chain.domain.block.nested.BlockHash
import io.openfuture.chain.domain.block.nested.MerkleHash
import io.openfuture.chain.domain.transaction.TransactionDto
import io.openfuture.chain.entity.Block

class BlockDto(
        val blockData: BlockData,
        val blockHash: BlockHash,
        val nodePublicKey: String,
        var nodeSignature: String
) {

    constructor(block: Block) : this(
            BlockData(block.timestamp, block.orderNumber, block.previousHash, MerkleHash(block.merkleHash,
                    block.transactions.map { TransactionDto(it) })),
            BlockHash(block.nonce, block.hash),
            block.nodeKey,
            block.nodeSignature
    )

}