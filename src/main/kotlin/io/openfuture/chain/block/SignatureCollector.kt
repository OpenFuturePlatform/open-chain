package io.openfuture.chain.block

import io.openfuture.chain.domain.block.PendingBlock
import io.openfuture.chain.domain.block.SignaturePublicKeyPair
import io.openfuture.chain.entity.Block
import io.openfuture.chain.service.BlockService
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class SignatureCollector(
    private val blockService: BlockService,
    @Value("\${block.time.slot}") private val slotInterval: Long
) {

    companion object {
        private const val APPROVAL_THRESHOLD = 0.67
    }

    private val scheduler = ThreadPoolTaskScheduler()
    private val signatures = ConcurrentHashMap.newKeySet<SignaturePublicKeyPair>()
    private var pendingBlock: Block? = null
    private var active: Boolean = false


    fun setPendingBlock(generatedBlock: PendingBlock) {
        if (!active) {
            scheduler.scheduleWithFixedDelay({ applyBlock() }, slotInterval / 2)
            this.pendingBlock = generatedBlock.block
            this.active = true
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
            if (signatures.size.toDouble() / genesisBlock.activeDelegateKeys.size > APPROVAL_THRESHOLD) {
                blockService.save(pendingBlock!!)
            }
        } finally {
            this.active = false
            signatures.clear()
        }
    }

}