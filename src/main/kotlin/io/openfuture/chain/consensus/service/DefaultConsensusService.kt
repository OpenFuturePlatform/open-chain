package io.openfuture.chain.consensus.service

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.service.CommonBlockService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultConsensusService(
    private val consensusProperties: ConsensusProperties,
    private val commonBlockService: CommonBlockService,
    private val genesisBlockService: GenesisBlockService
) : ConsensusService {

    @Transactional(readOnly = true)
    override fun getCurrentEpochHeight(): Long {
        val lastBlock = commonBlockService.getLast()
        val lastGenesisBlockHeight = genesisBlockService.getLast().height

        return lastBlock.height - lastGenesisBlockHeight
    }

    @Transactional(readOnly = true)
    override fun isGenesisBlockNeeded(): Boolean = (consensusProperties.epochHeight!! - 1) <= getCurrentEpochHeight()

}