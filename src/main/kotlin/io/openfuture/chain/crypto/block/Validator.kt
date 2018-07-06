package io.openfuture.chain.crypto.block

import io.openfuture.chain.entity.Block

interface Validator {

    fun isValid(block: Block): Boolean

    fun getVersion(): Int

}