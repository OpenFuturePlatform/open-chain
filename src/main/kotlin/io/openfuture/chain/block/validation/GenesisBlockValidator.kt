package io.openfuture.chain.block.validation

import io.openfuture.chain.entity.block.Block
import io.openfuture.chain.entity.block.BlockType
import org.springframework.stereotype.Component

@Component
class GenesisBlockValidator : BlockValidator {

    override fun isValid(block: Block): Boolean {
        return true
    }

    override fun getTypeId(): Int = BlockType.GENESIS.id

}