package io.openfuture.chain.consensus.service

import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.block.GenesisBlock

interface ConsensusService {

    fun getCurrentEpochHeight(): Long

    fun isGenesisBlockNeeded(): Boolean

}

interface EpochService {

    fun getEpochStart(): Long

    fun getDelegates(): Set<Delegate>

    fun getGenesisBlockHeight(): Long

    fun switchEpoch(genesisBlock: GenesisBlock)

    fun getCurrentSlotOwner(): Delegate

    fun getSlotNumber(): Long

    fun getSlotNumber(time: Long): Long

}