package io.openfuture.chain.consensus.service

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.network.component.node.NodeClock
import org.springframework.stereotype.Service
import java.util.*

@Service
class DefaultEpochService(
    genesisBlockService: GenesisBlockService,
    private val properties: ConsensusProperties,
    private val clock: NodeClock
) : EpochService {

    private var genesisBlock: GenesisBlock = genesisBlockService.getLast()


    override fun getEpochStart(): Long {
        return genesisBlock.timestamp
    }

    override fun getDelegates(): Set<Delegate> {
        return genesisBlock.activeDelegates
    }

    override fun getEpochIndex(): Long {
        return genesisBlock.epochIndex
    }

    override fun getGenesisBlockHeight(): Long {
        return genesisBlock.height
    }

    override fun switchEpoch(genesisBlock: GenesisBlock) {
        this.genesisBlock = genesisBlock
    }

    override fun getCurrentSlotOwner(): Delegate {
        val delegates = genesisBlock.activeDelegates
        val random = Random(genesisBlock.height + getSlotNumber())
        return delegates.shuffled(random).first()
    }

    override fun getSlotNumber(): Long {
        return ((clock.networkTime() - getEpochStart()) / properties.timeSlotDuration!!)
    }

    override fun getSlotNumber(time: Long): Long {
        return ((time - getEpochStart()) / properties.timeSlotDuration!!)
    }

}