package io.openfuture.chain.domain.block

import io.openfuture.chain.entity.block.Block

data class PendingBlock(
    val block: Block,
    val signature: Signature
)