package io.openfuture.chain.block.validation

import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.BlockType
import org.springframework.stereotype.Component

@Component
class GenesisBlockValidator : BlockValidator {

    override fun isValid(block: Block): Boolean {
        return true
    }

    override fun getTypeId(): Int {
        return BlockType.GENESIS.id
    }

}