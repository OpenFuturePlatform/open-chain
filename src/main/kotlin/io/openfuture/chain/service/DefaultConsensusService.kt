package io.openfuture.chain.service

import io.openfuture.chain.entity.Block
import io.openfuture.chain.property.ConsensusProperties
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultConsensusService(
    private val consensusProperties: ConsensusProperties,
    private val blockService: BlockService<Block>,
    private val genesisBlockService: DefaultGenesisBlockService
) : ConsensusService {

    @Transactional(readOnly = true)
    override fun getCurrentEpochHeight(): Long {
        val lastBlock = blockService.getLast()
        val lastGenesisBlockHeight = genesisBlockService.getLast().height

        return lastBlock.height - lastGenesisBlockHeight
    }

    @Transactional(readOnly = true)
    override fun isGenesisBlockNeeded(): Boolean = (consensusProperties.epochHeight!! - 1) <= getCurrentEpochHeight()

}