package io.openfuture.chain.core.sync

import io.openfuture.chain.core.model.entity.block.Block

interface SyncSession {

    fun isComplete(): Boolean

    fun getEpochForSync(): Long

    fun getStorage(): List<Block>

    fun add(epochBlocks: List<Block>): Boolean

}