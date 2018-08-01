package io.openfuture.chain.consensus.component.block

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.model.entity.block.BaseBlock
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.network.component.node.NodeClock
import org.springframework.stereotype.Component


@Component
class TimeSlot(
    private val properties: ConsensusProperties,
    private val clock: NodeClock,
    private val genesisBlockService: GenesisBlockService
) {

    fun getEpochTime(): Long {
        val genesis = genesisBlockService.getLast()
        return genesis.timestamp
    }

    fun getSlotTimestamp(): Long = getEpochTime() + getSlotTime()

    fun getSlotNumber(time: Long = clock.networkTime()): Long = ((time - getEpochTime()) / properties.timeSlotDuration!!)

    fun verifyTimeSlot(currentTime: Long, block: BaseBlock): Boolean = (getSlotNumber(currentTime) == getSlotNumber(block.timestamp))

    private fun getSlotTime(): Long = getSlotNumber() * properties.timeSlotDuration!!

}