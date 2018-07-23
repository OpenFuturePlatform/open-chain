package io.openfuture.chain.block.validation

import io.openfuture.chain.entity.block.Block

interface BlockValidator {

    fun isValid(block: Block): Boolean

    fun getTypeId(): Int

}