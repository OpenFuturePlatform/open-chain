package io.openfuture.chain.consensus.service

import io.openfuture.chain.core.model.entity.block.MainBlock

interface EpochService {

    fun getEpochStart(): Long

    fun getDelegatesPublicKeys(): List<String>

    fun getGenesisBlockHeight(): Long

    fun getEpochIndex(): Long

    fun getCurrentSlotOwner(): String

    fun isInIntermission(time: Long): Boolean

    fun timeToNextTimeSlot(): Long

    fun getSlotNumber(time: Long): Long

    fun getEpochEndTime(): Long

    fun getFullTimeSlotDuration(): Long

    fun getEpochByBlock(block: MainBlock): Long

}