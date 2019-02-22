package io.openfuture.chain.core.service.block.validation.pipeline

import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.util.BlockValidateHandler

class BlockValidationPipeline(
    val handlers: Array<BlockValidateHandler>
) {

    fun invoke(block: Block, lastBlock: Block, lastMainBlock: MainBlock, new: Boolean) {
        handlers.forEach { it.invoke(block, lastBlock, lastMainBlock, new) }
    }

}