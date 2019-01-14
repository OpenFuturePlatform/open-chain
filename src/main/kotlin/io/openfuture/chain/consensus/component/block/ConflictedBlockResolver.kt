package io.openfuture.chain.consensus.component.block

import io.openfuture.chain.consensus.service.EpochService
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.core.service.MainBlockService
import io.openfuture.chain.core.sync.ChainSynchronizer
import io.openfuture.chain.network.message.consensus.BlockAvailabilityRequest
import io.openfuture.chain.network.message.consensus.BlockAvailabilityResponse
import io.openfuture.chain.network.message.consensus.PendingBlockMessage
import io.openfuture.chain.network.service.NetworkApiService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class ConflictedBlockResolver(
    private val blockService: BlockService,
    private val genesisBlockService: GenesisBlockService,
    private val mainBlockService: MainBlockService,
    private val networkApiService: NetworkApiService,
    private val epochService: EpochService,
    private val chainSynchronizer: ChainSynchronizer
) {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ConflictedBlockResolver::class.java)
    }

    fun checkConflictedBlock(accepted: PendingBlockMessage) {
        if (!mainBlockService.verify(accepted)) {
            val delegate = epochService.getDelegates().random().toNodeInfo()
            val block = blockService.getLast()
            val message = BlockAvailabilityRequest(block.hash)
            networkApiService.sendToAddress(message, delegate)
        }
    }

    fun onBlockAvailabilityResponse(response: BlockAvailabilityResponse) {
        if (-1 == response.height) {
            val invalidGenesisBlock = genesisBlockService.getLast()
            log.info("Rolling back epoch # ${invalidGenesisBlock.payload.epochIndex}")
            blockService.removeEpoch(invalidGenesisBlock)
            val lastGenesisBlock = genesisBlockService.getLast()
            val delegate = epochService.getDelegates().random().toNodeInfo()
            val message = BlockAvailabilityRequest(lastGenesisBlock.hash)
            networkApiService.sendToAddress(message, delegate)
        } else {
            chainSynchronizer.sync()
        }
    }

}