package io.openfuture.chain.consensus.service

import io.openfuture.chain.core.model.entity.Delegate

interface EpochService {

    fun getEpochStart(): Long

    fun getDelegates(): List<Delegate>

    fun getGenesisBlockHeight(): Long

    fun getEpochIndex(): Long

    fun getCurrentSlotOwner(): Delegate

    fun getSlotNumber(): Long

    fun getSlotNumber(time: Long): Long

    fun getEpochEndTime(): Long

}