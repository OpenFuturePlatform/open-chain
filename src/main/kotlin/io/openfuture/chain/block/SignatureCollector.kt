package io.openfuture.chain.block

import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.domain.block.PendingBlock
import io.openfuture.chain.domain.block.Signature
import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.GenesisBlock
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.property.ConsensusProperties
import io.openfuture.chain.service.BlockService
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class SignatureCollector(
    private val mainBlockService: BlockService<MainBlock>,
    private val genesisBlockService: BlockService<GenesisBlock>,
    private val properties: ConsensusProperties,
    private val timeSlot: TimeSlot,
    private val clock: NodeClock
) {

    companion object {
        private const val APPROVAL_THRESHOLD = 0.67
    }

    private val scheduler = ThreadPoolTaskScheduler()
    private val signatures = ConcurrentHashMap.newKeySet<Signature>()
    private lateinit var pendingBlock: Block
    private var active: Boolean = false

    init {
        scheduler.initialize()
    }


    fun setPendingBlock(generatedBlock: PendingBlock) {
        if (!active) {
            this.active = true
            this.pendingBlock = generatedBlock.block
            scheduler.scheduleWithFixedDelay({ applyBlock() }, properties.timeSlotDuration!! / 2)
        }
    }

    fun addSignatureBlock(signatureBlock: PendingBlock): Boolean {
        if (signatureBlock.block.hash != pendingBlock.hash) {
            return false
        }

        return signatures.add(signatureBlock.signature)
    }

    fun applyBlock() {
        try {
            if (!timeSlot.verifyTimeSlot(clock.networkTime(), pendingBlock)) {
                return
            }

            val genesisBlock = genesisBlockService.getLast()
            if (signatures.size.toDouble() / genesisBlock.activeDelegates.size > APPROVAL_THRESHOLD) {
                if (pendingBlock is MainBlock) {
                    mainBlockService.save(pendingBlock as MainBlock)
                } else if (pendingBlock is GenesisBlock) {
                    genesisBlockService.save(pendingBlock as GenesisBlock)
                }
            }
        } finally {
            this.active = false
            signatures.clear()
        }
    }

}