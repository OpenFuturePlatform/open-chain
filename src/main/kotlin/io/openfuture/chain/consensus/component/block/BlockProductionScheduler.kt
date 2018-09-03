package io.openfuture.chain.consensus.component.block

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.consensus.service.EpochService
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.core.service.MainBlockService
import io.openfuture.chain.network.component.NodeClock
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.annotation.PostConstruct

@Component
class BlockProductionScheduler(
    private val keyHolder: NodeKeyHolder,
    private val epochService: EpochService,
    private val blockService: BlockService,
    private val mainBlockService: MainBlockService,
    private val genesisBlockService: GenesisBlockService,
    private val consensusProperties: ConsensusProperties,
    private val pendingBlockHandler: PendingBlockHandler,
    private val clock: NodeClock
) {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(BlockProductionScheduler::class.java)
    }

    private val executor: ExecutorService = Executors.newSingleThreadExecutor()


    @PostConstruct
    fun init() {
        executor.submit { proceedProductionLoop() }
    }

    private fun proceedProductionLoop() {
        while (true) {
            try {
                val slotOwner = epochService.getCurrentSlotOwner()
                if (isGenesisBlockRequired()) {
                    val block = blockService.getLast()
                    val nextTimeSlot = epochService.getSlotNumber(block.timestamp) + 1
                    val timestamp = epochService.getEpochStart() + epochService.getFullTimeSlotDuration() * nextTimeSlot
                    val genesisBlock = genesisBlockService.create(timestamp)
                    genesisBlockService.add(genesisBlock)
                    pendingBlockHandler.resetSlotNumber()
                } else if (keyHolder.getPublicKey() == slotOwner.publicKey) {
                    val block = mainBlockService.create()
                    pendingBlockHandler.addBlock(block)
                }
            } catch (ex: Exception) {
                log.error("Block creation failure inbound: ${ex.message}")
            } finally {
                Thread.sleep(epochService.timeToNextTimeSlot(clock.networkTime()))
            }
        }
    }

    private fun isGenesisBlockRequired(): Boolean {
        val blocksProduced = blockService.getLast().height - epochService.getGenesisBlockHeight()
        return (consensusProperties.epochHeight!! - 1) <= blocksProduced
    }

}