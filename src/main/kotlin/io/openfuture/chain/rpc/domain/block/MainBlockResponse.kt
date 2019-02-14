package io.openfuture.chain.rpc.domain.block

import io.openfuture.chain.core.model.entity.block.MainBlock

class MainBlockResponse(
    timestamp: Long,
    height: Long,
    previousHash: String,
    hash: String,
    signature: String,
    publicKey: String,
    val merkleHash: String,
    val transactionsCount: Long,
    val epochIndex: Long
) : BaseBlockResponse(timestamp, height, previousHash, hash, signature, publicKey) {

    constructor(block: MainBlock, transactionsCount: Long, epochIndex: Long) : this(
        block.timestamp,
        block.height,
        block.previousHash,
        block.hash,
        block.signature,
        block.publicKey,
        block.getPayload().transactionMerkleHash,
        transactionsCount,
        epochIndex
    )

}
