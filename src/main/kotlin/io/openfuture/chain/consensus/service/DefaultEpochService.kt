package io.openfuture.chain.consensus.service

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.network.component.node.NodeClock
import org.springframework.stereotype.Service
import java.util.*

@Service
class DefaultEpochService(
    private val genesisBlockService: GenesisBlockService,
    private val properties: ConsensusProperties,
    private val clock: NodeClock
) : EpochService {

    override fun getEpochStart(): Long = genesisBlockService.getLast().timestamp

    override fun getDelegates(): Set<Delegate> = genesisBlockService.getLast().activeDelegates

    override fun getEpochIndex(): Long = genesisBlockService.getLast().epochIndex

    override fun getGenesisBlockHeight(): Long = genesisBlockService.getLast().height

    override fun getCurrentSlotOwner(): Delegate {
        val genesisBlock = genesisBlockService.getLast()
        val delegates = genesisBlock.activeDelegates
        val random = Random(genesisBlock.height + getSlotNumber())
        return delegates.shuffled(random).first()
    }

    override fun getSlotNumber(): Long = ((clock.networkTime() - getEpochStart()) / properties.timeSlotDuration!!)

    override fun getSlotNumber(time: Long): Long = ((time - getEpochStart()) / properties.timeSlotDuration!!)

}