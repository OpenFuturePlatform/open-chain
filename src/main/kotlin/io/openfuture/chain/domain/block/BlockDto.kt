package io.openfuture.chain.domain.block

import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.domain.block.nested.BlockData
import io.openfuture.chain.domain.block.nested.MerkleHash
import io.openfuture.chain.domain.transaction.TransactionDto
import io.openfuture.chain.entity.Block

class BlockDto(
        val blockData: BlockData,
        val timestamp: Long,
        val hash: String,
        val nodePublicKey: String,
        var nodeSignature: String
) {

    companion object {
        fun of(networkTime: Long, data: BlockData, nodePublicKey: String, nodeSignature: String): BlockDto = BlockDto(
                data,
                networkTime,
                HashUtils.generateHash(data.getByteData() + networkTime.toByte()),
                nodePublicKey,
                nodeSignature
        )
    }

    constructor(block: Block) : this(
            BlockData(block.orderNumber, block.previousHash, MerkleHash(block.merkleHash,
                    block.transactions.map { TransactionDto(it) })),
            block.timestamp,
            block.hash,
            block.nodePublicKey,
            block.nodeSignature
    )

}