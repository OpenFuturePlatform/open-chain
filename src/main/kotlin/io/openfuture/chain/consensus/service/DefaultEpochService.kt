package io.openfuture.chain.consensus.service

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.service.BlockManager
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultEpochService(
    private val blockManager: BlockManager,
    private val properties: ConsensusProperties
) : EpochService {

    override fun getEpochStart(): Long = blockManager.getLastGenesisBlock().timestamp

    override fun getDelegatesPublicKeys(): List<String> = blockManager.getLastGenesisBlock().getPayload().activeDelegates

    override fun getEpochIndex(): Long = blockManager.getLastGenesisBlock().getPayload().epochIndex

    override fun getGenesisBlockHeight(): Long = blockManager.getLastGenesisBlock().height

    override fun getCurrentSlotOwner(): String {
        val genesisBlock = blockManager.getLastGenesisBlock()
        val activeDelegates = genesisBlock.getPayload().activeDelegates
        val slotNumber = getSlotNumber(System.currentTimeMillis())
        val mod = slotNumber % activeDelegates.size
        return if (mod != 0L) activeDelegates[mod.toInt() - 1] else activeDelegates.last()
    }

    override fun getEpochByBlock(block: MainBlock): Long =
        blockManager.getPreviousGenesisBlockByHeight(block.height).getPayload().epochIndex

    override fun isInIntermission(time: Long): Boolean = (getTimeSlotFromStart(time) >= properties.timeSlotDuration!!)

    override fun timeToNextTimeSlot(): Long = (getFullTimeSlotDuration() - getTimeSlotFromStart(System.currentTimeMillis()))

    override fun getSlotNumber(time: Long): Long = ((time - getEpochStart()) / getFullTimeSlotDuration())

    override fun getEpochEndTime(): Long =
        (getEpochStart() + getSlotNumber(System.currentTimeMillis()) * getFullTimeSlotDuration())

    override fun getFullTimeSlotDuration(): Long = (properties.timeSlotDuration!! + properties.timeSlotInterval!!)

    private fun getTimeSlotFromStart(time: Long): Long = (time - getEpochStart()) % getFullTimeSlotDuration()

}