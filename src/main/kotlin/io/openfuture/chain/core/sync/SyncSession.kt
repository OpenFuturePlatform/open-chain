package io.openfuture.chain.core.sync

import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.crypto.util.SignatureUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import java.util.*

open class SyncSession(
    private val lastLocalGenesisBlock: GenesisBlock,
    private val currentGenesisBlock: GenesisBlock
) {

    private val storage: SortedSet<Block> = TreeSet(kotlin.Comparator { o1, o2 -> (o1.height - o2.height).toInt() })

    fun isEpochSynced(): Boolean = storage.last().hash == lastLocalGenesisBlock.hash

    fun isComplete(): Boolean = storage.last().height > currentGenesisBlock.height

    fun getStorage(): SortedSet<Block> = storage

    fun getCurrentGenesisBlock(): GenesisBlock = currentGenesisBlock

    @Synchronized
    fun add(epochBlocks: List<Block>): Boolean {
        val list = epochBlocks.sortedByDescending { it.height }

        if (list.first() is GenesisBlock) {

        }

        if (isChainValid(list)) {
            storage.addAll(list)
            return true
        }

        return false
    }

    private fun isChainValid(chain: List<Block>): Boolean {
        if (!isValid(storage.last(), chain.first())) {
            return false
        }

        chain.forEachIndexed { idx, it ->
            if (!isValid(it, chain[idx + 1])) {
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