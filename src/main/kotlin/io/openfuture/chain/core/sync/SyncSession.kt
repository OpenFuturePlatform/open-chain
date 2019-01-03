package io.openfuture.chain.core.sync

import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.crypto.util.SignatureUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils

class SyncSession(
    latestGenesisBlock: GenesisBlock,
    val currentGenesisBlock: GenesisBlock
) {

    private val storage: MutableList<Block> = mutableListOf(latestGenesisBlock)

    fun isComplete(): Boolean = storage.last().height == currentGenesisBlock.height

    fun getStorage(): List<Block>  = storage.subList(0, storage.lastIndex)

    fun add(epochBlocks: List<Block>): Boolean {
        epochBlocks.forEach {
            if (!isValid(it)) {
                rollback(epochBlocks)
                return false
            }
            storage.add(it)
        }

        return true
    }

    private fun rollback(epochBlocks: List<Block>) {
        storage.removeAll(epochBlocks)
    }

    private fun isValid(block: Block): Boolean {
        val last = storage.last()

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