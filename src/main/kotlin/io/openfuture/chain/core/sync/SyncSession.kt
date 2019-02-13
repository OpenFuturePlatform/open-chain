package io.openfuture.chain.core.sync

import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.TemporaryBlock
import io.openfuture.chain.core.service.DefaultTemporaryBlockService
import io.openfuture.chain.core.util.SerializationUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class SyncSession(
    private val temporaryBlockService: DefaultTemporaryBlockService
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
        epochQuantity = currentGenesisBlock.payload.epochIndex - lastLocalGenesisBlock.payload.epochIndex
    }

    fun getCurrentGenesisBlock(): GenesisBlock = currentGenesisBlock

    fun getTemporaryBlocks(heights: List<Long>): List<Block> = temporaryBlockService.getByHeightIn(heights).map {
        SerializationUtils.deserialize(ByteUtils.fromHexString(it.block)) as Block
    }

    @Synchronized
    fun add(epochBlocks: List<Block>): Boolean {
        if (!isChainValid(epochBlocks)) {
            return false
        }

        if (!epochBlocks.any { it.height >= minBlock.height }) {
            return true
        }

        val temporaryBlocks = createTemporaryBlocks(epochBlocks)

        temporaryBlockService.save(temporaryBlocks)
        completed = null != epochBlocks.firstOrNull { it.hash == currentGenesisBlock.hash }
        epochAdded++
        log.info("#$epochAdded epochs FROM ${epochQuantity + 1} is processed")

        for (block in epochBlocks) {
            if (minBlock.height > block.height) {
                minBlock = block
            }
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

    private fun createTemporaryBlocks(blocks: List<Block>): List<TemporaryBlock> =
        blocks.map { TemporaryBlock(it.height, ByteUtils.toHexString(SerializationUtils.serialize(it))) }

    private fun isChainValid(chain: List<Block>): Boolean {
        val list = if (chain.first().hash != currentGenesisBlock.hash) {
            chain.sortedByDescending { it.height }.toMutableList().apply { add(0, minBlock) }
        } else {
            chain.sortedByDescending { it.height }
        }

        for (idx in 0 until list.size - 2) {
            if (!isValid(list[idx], list[idx + 1])) {
                return false
            }
        }

        return true
    }

    private fun isValid(last: Block, block: Block): Boolean {

        if (last.previousHash != block.hash) {
            return false
        }

        if (last.height != block.height + 1) {
            return false
        }

        if (last.timestamp < block.timestamp) {
            return false
        }

        val hash = ByteUtils.fromHexString(block.hash)
        val publicKey = ByteUtils.fromHexString(block.publicKey)

        if (block.height != 1L && !SignatureUtils.verify(hash, block.signature, publicKey)) {
            return false
        }

        return true
    }

}