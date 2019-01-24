package io.openfuture.chain.consensus.service

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.service.GenesisBlockService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultEpochService(
    private val genesisBlockService: GenesisBlockService,
    private val properties: ConsensusProperties
) : EpochService {

    override fun getEpochStart(): Long = genesisBlockService.getLast().timestamp

    override fun getDelegates(): List<Delegate> = genesisBlockService.getLast().payload.activeDelegates

    override fun getEpochIndex(): Long = genesisBlockService.getLast().payload.epochIndex

    override fun getGenesisBlockHeight(): Long = genesisBlockService.getLast().height

    @Transactional(readOnly = true)
    override fun getCurrentSlotOwner(): Delegate {
        val genesisBlock = genesisBlockService.getLast()
        val activeDelegates = genesisBlock.payload.activeDelegates
        val slotNumber = getSlotNumber(System.currentTimeMillis())
        val mod = slotNumber % activeDelegates.size
        return if (mod != 0L) activeDelegates[mod.toInt() - 1] else activeDelegates.last()
    }

    override fun getEpochByBlock(block: MainBlock): Long =
        genesisBlockService.getPreviousByHeight(block.height).payload.epochIndex

    override fun isInIntermission(time: Long): Boolean = (getTimeSlotFromStart(time) >= properties.timeSlotDuration!!)

    override fun timeToNextTimeSlot(): Long = (getFullTimeSlotDuration() - getTimeSlotFromStart(System.currentTimeMillis()))

    override fun getSlotNumber(time: Long): Long = ((time - getEpochStart()) / getFullTimeSlotDuration())

    override fun getEpochEndTime(): Long =
        (getEpochStart() + getSlotNumber(System.currentTimeMillis()) * getFullTimeSlotDuration())

    override fun getFullTimeSlotDuration(): Long = (properties.timeSlotDuration!! + properties.timeSlotInterval!!)

    private fun getTimeSlotFromStart(time: Long): Long = (time - getEpochStart()) % getFullTimeSlotDuration()

}