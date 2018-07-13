package io.openfuture.chain.domain.block

import io.openfuture.chain.entity.Block

data class PendingBlock(
    val block: Block,
    val signature: Signature
)