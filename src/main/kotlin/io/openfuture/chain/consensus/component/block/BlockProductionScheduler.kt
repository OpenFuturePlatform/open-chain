package io.openfuture.chain.consensus.component.block

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.consensus.service.EpochService
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.core.service.MainBlockService
import io.openfuture.chain.network.component.node.NodeClock
import org.springframework.stereotype.Component
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

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

    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    private var currentTimeSlot: Long = 0


    @PostConstruct
    fun init() {
        executor.submit {
            while (true) {
                val networkTime = clock.networkTime()
                val timeSlot = epochService.getSlotNumber(networkTime)
                if (!epochService.isInTimeSlot(networkTime) || timeSlot <= currentTimeSlot) {
                    Thread.sleep(epochService.timeToNextTimeSlot(networkTime))
                    continue
                }

                currentTimeSlot = timeSlot
                val slotOwner = epochService.getCurrentSlotOwner()
                if (isGenesisBlockRequired()) {
                    val genesisBlock = genesisBlockService.create()
                    genesisBlock.timestamp = epochService.getEpochEndTime()
                    genesisBlockService.add(genesisBlock)
                } else if (keyHolder.getPublicKey() == slotOwner.publicKey) {
                    val block = mainBlockService.create()
                    pendingBlockHandler.addBlock(block)
                }
            }
        }
    }

    @PreDestroy
    fun shutdown() {
        executor.shutdown()
    }

    private fun isGenesisBlockRequired(): Boolean {
        val blocksProduced = blockService.getLast().height - epochService.getGenesisBlockHeight()
        return (consensusProperties.epochHeight!! - 1) <= blocksProduced
    }

}