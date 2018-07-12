package io.openfuture.chain.service

import io.openfuture.chain.property.ConsensusProperties
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultConsensusService(
    private val consensusProperties: ConsensusProperties,
    private val blockService: BlockService
) : ConsensusService {

    @Transactional(readOnly = true)
    override fun getCurrentEpochHeight(): Long {
        val newHeight = blockService.getLastMain().height
        val lastGenesisBlockHeight = blockService.getLastGenesis().height

        return newHeight - lastGenesisBlockHeight
    }

    @Transactional(readOnly = true)
    override fun isGenesisBlockNeeded(): Boolean
        = (consensusProperties.epochHeight!! - 1) <= getCurrentEpochHeight()

}