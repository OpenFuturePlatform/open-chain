package io.openfuture.chain.service

import io.openfuture.chain.property.EpochProperties
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultEpochService(
        private val epochProperties: EpochProperties
) : EpochService {

    override fun getActualSizeCurrentEpoch(): Int {
        val orderNumberLastBlock = 130 // TODO("blockService.getLastBlock().orderNumber")
        val orderNumberLastGenesisBlock = 100 // TODO("genesisBlockService.getLastBlock().orderNumber")

        return orderNumberLastBlock - orderNumberLastGenesisBlock
    }

    override fun isCreateGenesisBlock(): Boolean {
        return (epochProperties.size!! - 1) == getActualSizeCurrentEpoch()
    }

}