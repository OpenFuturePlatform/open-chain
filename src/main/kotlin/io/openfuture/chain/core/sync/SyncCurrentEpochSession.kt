package io.openfuture.chain.core.sync

import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.crypto.util.SignatureUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils

class SyncCurrentEpochSession(
    val currentGenesisBlock: GenesisBlock
) : SyncSession {

    private val storage: MutableSet<Block> = mutableSetOf(currentGenesisBlock)


    override fun getEpochForSync(): Long = currentGenesisBlock.payload.epochIndex

    override fun getStorage(): List<Block> = storage.filter { it.hash != currentGenesisBlock.hash }.sortedBy { it.height }

    override fun isComplete(): Boolean = true

    @Synchronized
    override fun add(epochBlocks: List<Block>): Boolean {
        val list = epochBlocks.sortedBy { it.height }

        if (!isValid(storage.last(), list.first())) {
            return false
        }

        if (isChainValid(list)) {
            storage.addAll(list)
            return true
        }

        return false
    }

    private fun isChainValid(chain: List<Block>): Boolean {
        for (index in 0 until chain.size - 1) {
            if (!isValid(chain[index], chain[index + 1])) {
                return false
            }
        }
        return true
    }

    private fun isValid(last: Block, block: Block): Boolean {

        if (last.hash != block.previousHash) {
            return false
        }

        if (last.height + 1 != block.height) {
            return false
        }

        if (last.timestamp > block.timestamp) {
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