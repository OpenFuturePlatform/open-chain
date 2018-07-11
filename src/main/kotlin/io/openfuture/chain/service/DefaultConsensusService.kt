package io.openfuture.chain.service

import io.openfuture.chain.property.ConsensusProperties
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultConsensusService(
    private val consensusProperties: ConsensusProperties
) : ConsensusService {

    override fun getCurrentEpochHeight(): Int {
        val newHeight = 130 // TODO("blockService.getLast().height")
        val lastGenesisBlockHeight = 100 // TODO("blockService.getLastGenesis().height")

        return newHeight - lastGenesisBlockHeight
    }

    override fun isGenesisBlockNeeded(): Boolean {
        return (consensusProperties.epochHeight!! - 1) == getCurrentEpochHeight()
    }

}