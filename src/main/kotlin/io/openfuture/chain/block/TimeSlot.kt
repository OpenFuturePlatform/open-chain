package io.openfuture.chain.block

import io.openfuture.chain.component.node.NodeClock
import io.openfuture.chain.entity.Block
import io.openfuture.chain.property.ConsensusProperties
import io.openfuture.chain.service.BlockService
import org.springframework.stereotype.Component


@Component
class TimeSlot(
    private val properties: ConsensusProperties,
    private val clock: NodeClock,
    private val blockService: BlockService
) {

    fun getEpochTime(): Long {
        val genesis = blockService.getLastGenesis()
        return genesis.epochIndex
    }

    fun getSlotTimestamp(time: Long = clock.networkTime()): Long {
        return time + getSlotTime()
    }

    fun getSlotTime(): Long {
        return getEpochTime() + getSlotNumber() * properties.timeSlotDuration!!
    }

    fun getSlotNumber(time: Long = clock.networkTime()): Long
        = ((time - getEpochTime()) / properties.timeSlotDuration!!)

    fun verifyTimeSlot(currentTime: Long, block: Block) = (getSlotNumber(currentTime) == getSlotNumber(block.timestamp))

}