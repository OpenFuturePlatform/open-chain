package io.openfuture.chain.core.sync

import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.crypto.util.SignatureUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils

class SyncCurrentEpochSession(
    currentGenesisBlock: GenesisBlock
) : SyncSession {

    private val storage: MutableSet<Block> = mutableSetOf(currentGenesisBlock)


    override fun getLastBlock(): Block = storage.last()

    override fun getStorage(): List<Block> = storage.toList()

    override fun isComplete(): Boolean = true

    override fun add(epochBlocks: List<Block>): Boolean {
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