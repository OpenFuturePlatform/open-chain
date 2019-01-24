package io.openfuture.chain.core.sync

import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.crypto.util.SignatureUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import java.util.*

class SyncSession(
    val syncMode: SyncMode,
    private val lastLocalGenesisBlock: GenesisBlock,
    private val currentGenesisBlock: GenesisBlock
) {

    private val storage: SortedSet<Block> = TreeSet(kotlin.Comparator { o1, o2 -> (o2.height - o1.height).toInt() })
    private var completed: Boolean = false

    init {
        storage.add(currentGenesisBlock)
    }


    fun isEpochSynced(): Boolean = storage.last().hash == lastLocalGenesisBlock.hash

    fun isCompleted(): Boolean = completed

    fun getStorage(): SortedSet<Block> = storage

    fun getCurrentGenesisBlock(): GenesisBlock = currentGenesisBlock

    fun getLastLocalGenesisBlock() = lastLocalGenesisBlock

    @Synchronized
    fun add(epochBlocks: List<Block>): Boolean {
        if (isChainValid(epochBlocks)) {
            storage.addAll(epochBlocks)
            completed = null != epochBlocks.firstOrNull { it.hash == currentGenesisBlock.hash }
            return true
        }

        return false
    }

    private fun isChainValid(chain: List<Block>): Boolean {
        val list = if (chain.first().hash != currentGenesisBlock.hash) {
            chain.sortedByDescending { it.height }.toMutableList().apply { add(0, storage.last()) }
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