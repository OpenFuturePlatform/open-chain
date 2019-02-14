package io.openfuture.chain.rpc.domain.block

import io.openfuture.chain.core.model.entity.block.GenesisBlock

class GenesisBlockResponse(
    timestamp: Long,
    height: Long,
    previousHash: String,
    hash: String,
    signature: String,
    publicKey: String,
    val epochIndex: Long,
    val delegatesCount: Int
) : BaseBlockResponse(timestamp, height, previousHash, hash, signature, publicKey) {

    constructor(block: GenesisBlock) : this(
        block.timestamp,
        block.height,
        block.previousHash,
        block.hash,
        block.signature,
        block.publicKey,
        block.getPayload().epochIndex,
        block.getPayload().activeDelegates.size
    )

}