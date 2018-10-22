package io.openfuture.chain.consensus.service

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.network.component.time.Clock
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class DefaultEpochService(
    private val genesisBlockService: GenesisBlockService,
    private val properties: ConsensusProperties,
    private val clock: Clock
) : EpochService {

    override fun getEpochStart(): Long = genesisBlockService.getLast().timestamp

    override fun getDelegates(): List<Delegate> = genesisBlockService.getLast().payload.activeDelegates

    override fun getEpochIndex(): Long = genesisBlockService.getLast().payload.epochIndex

    override fun getGenesisBlockHeight(): Long = genesisBlockService.getLast().height

    @Transactional(readOnly = true)
    override fun getCurrentSlotOwner(): Delegate {
        val genesisBlock = genesisBlockService.getLast()
        val random = Random(genesisBlock.height + getSlotNumber(clock.currentTimeMillis()))
        return genesisBlock.payload.activeDelegates.shuffled(random).first()
    }

    override fun getEpochByBlock(block: MainBlock): Long =
        genesisBlockService.getPreviousByHeight(block.height).payload.epochIndex

    override fun isInIntermission(time: Long): Boolean = (getTimeSlotFromStart(time) >= properties.timeSlotDuration!!)

    override fun timeToNextTimeSlot(): Long = (getFullTimeSlotDuration() - getTimeSlotFromStart(clock.currentTimeMillis()))

    override fun getSlotNumber(time: Long): Long = ((time - getEpochStart()) / getFullTimeSlotDuration())

    override fun getEpochEndTime(): Long =
        (getEpochStart() + getSlotNumber(clock.currentTimeMillis()) * getFullTimeSlotDuration())

    override fun getFullTimeSlotDuration(): Long = (properties.timeSlotDuration!! + properties.timeSlotInterval!!)

    private fun getTimeSlotFromStart(time: Long): Long = (time - getEpochStart()) % getFullTimeSlotDuration()

}