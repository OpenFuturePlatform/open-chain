package io.openfuture.chain.consensus.component.block

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.consensus.service.EpochService
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.core.service.MainBlockService
import io.openfuture.chain.core.sync.SyncState
import io.openfuture.chain.core.sync.SyncState.SyncStatusType.SYNCHRONIZED
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

@Component
class BlockProductionScheduler(
    private val keyHolder: NodeKeyHolder,
    private val epochService: EpochService,
    private val mainBlockService: MainBlockService,
    private val genesisBlockService: GenesisBlockService,
    private val pendingBlockHandler: PendingBlockHandler,
    private val consensusProperties: ConsensusProperties,
    private val syncState: SyncState
) {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(BlockProductionScheduler::class.java)
    }

    private lateinit var executor: ScheduledExecutorService


    @PostConstruct
    fun onSyncReady() {
        executor = Executors.newSingleThreadScheduledExecutor()
        executor.scheduleAtFixedRate({ proceedProductionLoop() }, epochService.timeToNextTimeSlot(),
            consensusProperties.getPeriod(), TimeUnit.MILLISECONDS)
    }

    private fun proceedProductionLoop() {
        try {
            if (SYNCHRONIZED != syncState.getNodeStatus()) {
                log.warn("Node is not synchronized")
                return
            }
            val slotOwner = epochService.getCurrentSlotOwner()
            if (genesisBlockService.isGenesisBlockRequired()) {
                val genesisBlock = genesisBlockService.create()
                genesisBlockService.add(genesisBlock)
                pendingBlockHandler.resetSlotNumber()
            } else if (keyHolder.getPublicKeyAsHexString() == slotOwner.publicKey) {
                val block = mainBlockService.create()
                pendingBlockHandler.addBlock(block)
            }
        } catch (ex: Exception) {
            log.error("Block creation failure inbound: ${ex.message}")
        }
    }

}