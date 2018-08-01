package io.openfuture.chain.consensus.model.dto.block

import io.openfuture.chain.core.model.entity.block.BaseBlock

data class PendingBlock(
    val block: BaseBlock,
    val signature: BlockSignature
)