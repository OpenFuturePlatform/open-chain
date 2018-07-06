package io.openfuture.chain.block

import io.openfuture.chain.entity.Block

interface BlockValidator {

    fun isValid(block: Block): Boolean

    fun getVersion(): Int

}