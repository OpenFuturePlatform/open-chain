package io.openfuture.chain.consensus.component.block

import io.openfuture.chain.consensus.model.entity.Delegate
import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.consensus.service.GenesisBlockService
import io.openfuture.chain.network.component.node.NodeClock
import org.springframework.stereotype.Component
import java.util.*

@Component
class TimeSlotHelper(
    private val properties: ConsensusProperties,
    private val clock: NodeClock,
    private val genesisBlockService: GenesisBlockService
) {

    fun getEpochTime(): Long {
        val genesis = genesisBlockService.getLast()
        return genesis.timestamp
    }

    fun getCurrentSlotOwner(): Delegate {
        val genesisBlock = genesisBlockService.getLast()
        val delegates = genesisBlock.activeDelegates
        val random = Random(genesisBlock.height + getSlotNumber())
        return delegates.shuffled(random).first()
    }

    fun getSlotNumber(time: Long = clock.networkTime()): Long
        = ((time - getEpochTime()) / properties.timeSlotDuration!!)

}