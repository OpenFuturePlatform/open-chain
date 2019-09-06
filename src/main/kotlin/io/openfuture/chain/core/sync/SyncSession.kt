package io.openfuture.chain.core.sync

import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.block.TemporaryBlock
import io.openfuture.chain.core.service.DefaultTemporaryBlockService
import io.openfuture.chain.core.service.block.validation.MainBlockValidator
import io.openfuture.chain.core.service.block.validation.pipeline.BlockValidationPipeline
import io.openfuture.chain.core.sync.SyncMode.FULL
import io.openfuture.chain.core.sync.SyncMode.LIGHT
import io.openfuture.chain.core.util.SerializationUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class SyncSession(
    private val temporaryBlockService: DefaultTemporaryBlockService,
    private val mainBlockValidator: MainBlockValidator
) {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(SyncSession::class.java)
    }

    private lateinit var lastLocalGenesisBlock: GenesisBlock
    private lateinit var currentGenesisBlock: GenesisBlock
    private var epochQuantity: Long = 0L

    private var completed: Boolean = false
    private var epochAdded: Long = 0

    lateinit var minBlock: Block
    lateinit var syncMode: SyncMode


    fun isEpochSynced(): Boolean = minBlock.hash == lastLocalGenesisBlock.hash

    fun isCompleted(): Boolean = completed

    fun getEpochAdded(): Long = epochAdded

    @Synchronized
    fun init(syncMode: SyncMode, lastLocalGenesisBlock: GenesisBlock, currentGenesisBlock: GenesisBlock) {
        this.syncMode = syncMode
        this.lastLocalGenesisBlock = lastLocalGenesisBlock
        this.currentGenesisBlock = currentGenesisBlock
        this.minBlock = currentGenesisBlock
        epochQuantity = currentGenesisBlock.getPayload().epochIndex - lastLocalGenesisBlock.getPayload().epochIndex
    }

    fun getCurrentGenesisBlock(): GenesisBlock = currentGenesisBlock

    fun getTemporaryBlocks(heights: List<Long>): List<Block> = temporaryBlockService.getByHeightIn(heights).map {
        SerializationUtils.deserialize(ByteUtils.fromHexString(it.block)) as Block
    }

    @Synchronized
    fun add(epochBlocks: List<Block>): Boolean {
        epochBlocks.sortedBy { it.height }

        if (!isChainValid(epochBlocks)) {
            return false
        }

        val temporaryBlocks = createTemporaryBlocks(epochBlocks)

        try {
            temporaryBlockService.save(temporaryBlocks)
        } catch (e: Exception) {
            log.warn("Blocks till height ${epochBlocks.last().height} already saved, skiping...")
        }

        completed = null != epochBlocks.firstOrNull { it.hash == currentGenesisBlock.hash }
        epochAdded++
        log.info("#$epochAdded epochs FROM ${epochQuantity + 1} is processed")

        if (minBlock.height > epochBlocks.first().height) {
            minBlock = epochBlocks.first()
        }

        return true
    }

    @Synchronized
    fun clear() {
        temporaryBlockService.deleteAll()
        completed = false
        minBlock = currentGenesisBlock
        epochAdded = 0
    }

    fun clearTemporaryBlocks(){
        temporaryBlockService.deleteAll()
    }

    private fun createTemporaryBlocks(blocks: List<Block>): List<TemporaryBlock> =
        blocks.map { TemporaryBlock(it.height, ByteUtils.toHexString(SerializationUtils.serialize(it))) }

    private fun isChainValid(chain: List<Block>): Boolean {
        if (chain.first().hash != currentGenesisBlock.hash) {
            chain.toMutableList().add(minBlock)
        }

        val pipeline = when (syncMode) {
            FULL -> BlockValidationPipeline(mainBlockValidator.checkFullOnSync())
            LIGHT -> BlockValidationPipeline(mainBlockValidator.checkLightOnSync())
        }

        for (index in 1 until chain.size) {
            if (!mainBlockValidator.verify(chain[index], chain[index - 1], chain[index] as MainBlock, false, pipeline)) {
                return false
            }
        }

        return true
    }

}