package io.openfuture.chain.block

import io.openfuture.chain.domain.block.PendingBlock
import io.openfuture.chain.domain.block.Signature
import io.openfuture.chain.entity.block.Block
import io.openfuture.chain.entity.block.GenesisBlock
import io.openfuture.chain.entity.block.MainBlock
import io.openfuture.chain.property.ConsensusProperties
import io.openfuture.chain.service.BlockService
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class SignatureCollector(
    private val blockService: BlockService,
    private val properties: ConsensusProperties
) {

    companion object {
        private const val APPROVAL_THRESHOLD = 0.67
    }

    private val scheduler = ThreadPoolTaskScheduler()
    private val signatures = ConcurrentHashMap.newKeySet<Signature>()
    private var pendingBlock: Block? = null
    private var active: Boolean = false


    fun setPendingBlock(generatedBlock: PendingBlock) {
        if (!active) {
            this.active = true
            this.pendingBlock = generatedBlock.block
            scheduler.scheduleWithFixedDelay({ applyBlock() }, properties.timeSlotDuration!! / 2)
        }
    }

    fun addBlockSignature(blockSignature: PendingBlock): Boolean {
        if (blockSignature.block.hash != pendingBlock!!.hash) {
            return false
        }

        return signatures.add(blockSignature.signature)
    }

    fun applyBlock() {
        try {
            val genesisBlock = blockService.getLastGenesis()
            if (signatures.size.toDouble() / genesisBlock.activeDelegates.size > APPROVAL_THRESHOLD) {
                if (pendingBlock is MainBlock) {
                    blockService.save(pendingBlock!! as MainBlock)
                } else if (pendingBlock is GenesisBlock) {
                    blockService.save(pendingBlock!! as GenesisBlock)
                }
            }
        } finally {
            this.active = false
            signatures.clear()
        }
    }

}