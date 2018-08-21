package io.openfuture.chain.rpc.domain.block

import io.openfuture.chain.core.model.entity.block.GenesisBlock

class GenesisBlockResponse(
    timestamp: Long,
    height: Long,
    previousHash: String,
    reward: Long,
    hash: String,
    signature: String,
    publicKey: String,
    var epochIndex: Long,
    var delegatesCount: Int
) : BaseBlockResponse(timestamp, height, previousHash, reward, hash, signature, publicKey) {

    constructor(block: GenesisBlock) : this(
        block.timestamp,
        block.height,
        block.previousHash,
        block.reward,
        block.hash,
        block.signature,
        block.publicKey,
        block.payload.epochIndex,
        block.payload.activeDelegates.size
    )

}