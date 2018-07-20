package io.openfuture.chain.service

import io.openfuture.chain.property.ConsensusProperties
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultConsensusService(
    private val consensusProperties: ConsensusProperties,
    private val mainBlockService: DefaultMainBlockService,
    private val genesisBlockService: DefaultGenesisBlockService
) : ConsensusService {

    @Transactional(readOnly = true)
    override fun getCurrentEpochHeight(): Long {
        val newHeight = mainBlockService.getLast().height
        val lastGenesisBlockHeight = genesisBlockService.getLast().height

        return newHeight - lastGenesisBlockHeight
    }

    @Transactional(readOnly = true)
    override fun isGenesisBlockNeeded(): Boolean = (consensusProperties.epochHeight!! - 1) <= getCurrentEpochHeight()

}