package io.openfuture.chain.block

import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.entity.Block
import io.openfuture.chain.property.ConsensusProperties
import io.openfuture.chain.service.DefaultGenesisBlockService
import org.springframework.stereotype.Component


@Component
class TimeSlot(
    private val properties: ConsensusProperties,
    private val clock: NodeClock,
    private val genesisBlockService: DefaultGenesisBlockService
) {

    fun getEpochTime(): Long {
        val genesis = genesisBlockService.findLast()
        return genesis.timestamp
    }

    fun getSlotTimestamp(): Long {
        return getEpochTime() + getSlotTime()
    }

    fun getSlotTime(): Long {
        return getSlotNumber() * properties.timeSlotDuration!!
    }

    fun getSlotNumber(time: Long = clock.networkTime()): Long
        = ((time - getEpochTime()) / properties.timeSlotDuration!!)

    fun verifyTimeSlot(currentTime: Long, block: Block) = (getSlotNumber(currentTime) == getSlotNumber(block.timestamp))

}