package io.openfuture.chain.service

import io.openfuture.chain.property.EpochProperties
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultEpochService(
        private val epochProperties: EpochProperties
) : EpochService {

    override fun calculateHeightBlocksNextEpoch(): Int {
        val countActiveDelegates = 21 // TODO("delegateProperties.count")

        return epochProperties.securityParameter!! * countActiveDelegates
    }

    override fun getActualHeightBlocksCurrentEpoch(): Int {
        val orderNumberLastBlock = 130 // TODO("blockService.getLastBlock().orderNumber")
        val orderNumberLastGenesisBlock = 100 // TODO("genesisBlockService.getLastBlock().orderNumber")

        return (orderNumberLastBlock + 1) - orderNumberLastGenesisBlock
    }

    override fun isNewEpoch(): Boolean {
        val heightBlocksCurrentEpoch = 100 // TODO("genesisBlockService.getLastBlock().heightBlocks")
        val actualHeightBlocksCurrentEpoch = getActualHeightBlocksCurrentEpoch()

        return heightBlocksCurrentEpoch == actualHeightBlocksCurrentEpoch
    }

}