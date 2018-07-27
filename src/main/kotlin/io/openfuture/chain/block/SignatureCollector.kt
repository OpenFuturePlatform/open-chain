package io.openfuture.chain.block

import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.domain.block.PendingBlock
import io.openfuture.chain.domain.block.Signature
import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.GenesisBlock
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.property.ConsensusProperties
import io.openfuture.chain.service.GenesisBlockService
import io.openfuture.chain.service.MainBlockService
import org.springframework.scheduling.TaskScheduler
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Component
class SignatureCollector(
    private val mainBlockService: MainBlockService,
    private val genesisBlockService: GenesisBlockService,
    private val properties: ConsensusProperties,
    private val timeSlot: TimeSlot,
    private val clock: NodeClock,
    private val scheduler: TaskScheduler
) {

    companion object {
        private const val APPROVAL_THRESHOLD = 0.67
    }

    private val signatures = ConcurrentHashMap.newKeySet<Signature>()
    private lateinit var pendingBlock: Block
    private var active: Boolean = false


    fun setPendingBlock(generatedBlock: PendingBlock) {
        if (!active) {
            this.pendingBlock = generatedBlock.block
            this.active = true
            val applyDate = Date(timeSlot.getSlotTimestamp() + properties.timeSlotDuration!! / 2)
            scheduler.schedule({ applyBlock() }, applyDate)
        }
    }

    fun addSignatureBlock(signatureBlock: PendingBlock): Boolean {
        if (!active || signatureBlock.block.hash != pendingBlock.hash) {
            return false
        }

        return signatures.add(signatureBlock.signature)
    }

    fun applyBlock() {
        try {
            while (timeSlot.verifyTimeSlot(clock.networkTime(), pendingBlock)) {
                val genesisBlock = genesisBlockService.getLast()
                if (signatures.size.toDouble() / genesisBlock.activeDelegates.size > APPROVAL_THRESHOLD) {
                    if (pendingBlock is MainBlock) {
                        mainBlockService.save(pendingBlock as MainBlock)
                    } else if (pendingBlock is GenesisBlock) {
                        genesisBlockService.save(pendingBlock as GenesisBlock)
                    }
                    break
                } else {
                    Thread.sleep(properties.timeSlotWaitDuration!!)
                }
            }
        } finally {
            this.active = false
            signatures.clear()
        }
    }

}