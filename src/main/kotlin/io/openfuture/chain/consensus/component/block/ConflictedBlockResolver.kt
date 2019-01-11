package io.openfuture.chain.consensus.component.block

import io.openfuture.chain.consensus.service.EpochService
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.core.service.MainBlockService
import io.openfuture.chain.network.message.consensus.BlockAvailabilityRequest
import io.openfuture.chain.network.message.consensus.BlockAvailabilityResponse
import io.openfuture.chain.network.message.consensus.PendingBlockMessage
import io.openfuture.chain.network.service.NetworkApiService
import org.springframework.stereotype.Component

@Component
class ConflictedBlockResolver(
    private val mainBlockService: MainBlockService,
    private val networkApiService: NetworkApiService,
    private val epochService: EpochService,
    private val genesisBlockService: GenesisBlockService
) {

    fun checkConflictedBlock(accepted: PendingBlockMessage, conflicted: PendingBlockMessage) {
        if (!mainBlockService.verify(accepted)) {
            val delegate = epochService.getDelegates().random().toNodeInfo()
            val message = BlockAvailabilityRequest(conflicted.hash)
            networkApiService.sendToAddress(message, delegate)
        }
    }

    fun onBlockAvailabilityResponse(response: BlockAvailabilityResponse) {
        if (-1 == response.height) {
            val lastGenesisBlock = genesisBlockService.getLast()
            val delegate = epochService.getDelegates().random().toNodeInfo()
            val message = BlockAvailabilityRequest(lastGenesisBlock.hash)
            networkApiService.sendToAddress(message, delegate)
        } else {

        }
    }

}