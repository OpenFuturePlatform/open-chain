package io.openfuture.chain.core.sync

import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.crypto.util.SignatureUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils

open class DefaultSyncSession(
    latestGenesisBlock: GenesisBlock,
    private val currentGenesisBlock: GenesisBlock
) : SyncSession {

    private val storage: MutableSet<Block> = mutableSetOf(latestGenesisBlock)


    override fun isComplete(): Boolean = storage.last().height == currentGenesisBlock.height

    override fun getEpochForSync(): Long = (storage.last() as GenesisBlock).payload.epochIndex

    override fun getStorage(): List<Block> = storage.filter { it.hash != currentGenesisBlock.hash }.sortedBy { it.height }

    @Synchronized
    override fun add(epochBlocks: List<Block>): Boolean {
        val list = epochBlocks.sortedByDescending { it.height }

        if (!isValid(storage.last(), list.first())) {
            return false
        }

        for (index in 0 until list.size - 1) {
            if (!isValid(list[index], list[index + 1])) {
                return false
            }
        }

        storage.addAll(list)

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