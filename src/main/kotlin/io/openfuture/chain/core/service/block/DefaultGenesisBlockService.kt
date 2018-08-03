package io.openfuture.chain.core.service.block

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.repository.BlockRepository
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.DefaultDelegateService
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.network.component.node.NodeClock
import io.openfuture.chain.network.domain.NetworkDelegate
import io.openfuture.chain.network.domain.NetworkGenesisBlock
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultGenesisBlockService(
    blockService: BlockService,
    private val repository: BlockRepository<GenesisBlock>,
    private val delegateService: DefaultDelegateService,
    private val clock: NodeClock,
    private val consensusProperties: ConsensusProperties,
    private val keyHolder: NodeKeyHolder
) : BaseBlockService(blockService), GenesisBlockService {

    @Transactional(readOnly = true)
    override fun getLast(): GenesisBlock = repository.findFirstByOrderByHeightDesc()
        ?: throw NotFoundException("Last block not found")

    override fun create(): NetworkGenesisBlock {
        val lastBlock = blockService.getLast()
        val height = lastBlock.height + 1
        val previousHash = lastBlock.hash!!
        val time = clock.networkTime()
        val reward = consensusProperties.rewardBlock!!
        val epochIndex = getLast().epochIndex + 1
        val delegates = delegateService.getActiveDelegates().map { it.toMessage() }.toMutableSet()

        return NetworkGenesisBlock(height, previousHash, time, reward, epochIndex, delegates)
            .sign(ByteUtils.toHexString(keyHolder.getPublicKey()), keyHolder.getPrivateKey())
    }

    @Transactional
    override fun add(dto: NetworkGenesisBlock) {
        if (!isValid(dto)) {
            return
        }

        val block = repository.findOneByHash(dto.hash!!)
        if (null != block) {
            return
        }

        repository.save(GenesisBlock.of(dto))
        // todo broadcast
    }

    @Transactional
    override fun isValid(block: NetworkGenesisBlock): Boolean {
        val lastGenesisBlock = this.getLast().toMessage()
        return super.isValid(block)
            && !block.activeDelegates.isEmpty()
            && isValidateActiveDelegates(block.activeDelegates)
            && isValidEpochIndex(lastGenesisBlock, block)
    }

    private fun isValidateActiveDelegates(activeDelegates: MutableSet<NetworkDelegate>): Boolean {
        return activeDelegates == delegateService.getActiveDelegates().map { it.toMessage() }.toMutableSet()
    }

    private fun isValidEpochIndex(lastBlock: NetworkGenesisBlock, block: NetworkGenesisBlock): Boolean {
        return (lastBlock.epochIndex + 1 == block.epochIndex)
    }

}