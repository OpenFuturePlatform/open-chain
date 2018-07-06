package io.openfuture.chain.crypto.block

import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.BlockVersion
import org.springframework.stereotype.Component

@Component
class GenesisValidator : Validator {

    override fun isValid(block: Block): Boolean {
        return true
    }

    override fun getVersion(): Int {
        return BlockVersion.GENESIS.version
    }

}