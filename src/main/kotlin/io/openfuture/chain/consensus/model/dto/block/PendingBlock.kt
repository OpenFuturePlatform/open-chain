package io.openfuture.chain.consensus.model.dto.block

import io.openfuture.chain.core.model.entity.block.Block

data class PendingBlock(
    val block: Block,
    val signature: BlockSignature
)