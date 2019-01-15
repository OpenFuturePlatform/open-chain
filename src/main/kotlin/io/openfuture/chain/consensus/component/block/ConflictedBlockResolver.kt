package io.openfuture.chain.consensus.component.block

import io.openfuture.chain.consensus.service.EpochService
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.core.service.MainBlockService
import io.openfuture.chain.core.sync.ChainSynchronizer
import io.openfuture.chain.core.sync.ScheduledSynchronizer
import io.openfuture.chain.network.message.consensus.BlockAvailabilityRequest
import io.openfuture.chain.network.message.consensus.BlockAvailabilityResponse
import io.openfuture.chain.network.message.consensus.PendingBlockMessage
import io.openfuture.chain.network.service.NetworkApiService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ScheduledFuture

@Component
class ConflictedBlockResolver(
    private val blockService: BlockService,
    private val genesisBlockService: GenesisBlockService,
    private val networkApiService: NetworkApiService,
    private val epochService: EpochService,
    private val chainSynchronizer: ChainSynchronizer,
    private val scheduledSynchronizer: ScheduledSynchronizer
) {

    private var future: ScheduledFuture<*>? = null

    fun checkLastBlock() {
        val block = blockService.getLast()
        checkBlock(block)
        future = scheduledSynchronizer.startRequestScheduler(future, Runnable { checkBlock(block) })
    }

    fun onBlockAvailabilityResponse(response: BlockAvailabilityResponse) {
        future?.cancel(true)
        if (-1 == response.height) {
            val invalidGenesisBlock = genesisBlockService.getLast()
            log.info("Rolling back epoch # ${invalidGenesisBlock.payload.epochIndex}")
            blockService.removeEpoch(invalidGenesisBlock)
            val lastGenesisBlock = genesisBlockService.getLast()
            checkBlock(lastGenesisBlock)
            future = scheduledSynchronizer.startRequestScheduler(future, Runnable { checkBlock(lastGenesisBlock) })
        } else {
            chainSynchronizer.sync()
        }
    }

    private fun checkBlock(block: Block) {
        val delegate = epochService.getDelegates().random().toNodeInfo()
        val message = BlockAvailabilityRequest(block.hash)
        networkApiService.sendToAddress(message, delegate)
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(ConflictedBlockResolver::class.java)
    }

}