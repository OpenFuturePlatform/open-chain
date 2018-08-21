package io.openfuture.chain.rpc.domain.block

import io.openfuture.chain.core.model.entity.block.MainBlock

class MainBlockResponse(
    timestamp: Long,
    height: Long,
    previousHash: String,
    hash: String,
    signature: String,
    publicKey: String,
    var merkleHash: String,
    var transactionsCount: Long,
    var epochIndex: Long
) : BaseBlockResponse(timestamp, height, previousHash, hash, signature, publicKey) {

    constructor(block: MainBlock, epochIndex: Long) : this(
        block.timestamp,
        block.height,
        block.previousHash,
        block.hash,
        block.signature,
        block.publicKey,
        block.payload.merkleHash,
        (block.payload.transferTransactions.size + block.payload.voteTransactions.size + block.payload.delegateTransactions.size).toLong(),
        epochIndex
    )

}
