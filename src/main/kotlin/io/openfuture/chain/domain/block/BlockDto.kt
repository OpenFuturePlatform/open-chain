package io.openfuture.chain.domain.block

import io.openfuture.chain.domain.block.nested.BlockData
import io.openfuture.chain.domain.block.nested.BlockHash

class BlockDto(
        val blockData: BlockData,
        val blockHash: BlockHash,
        val nodePublicKey: String,
        var nodeSignature: String
)