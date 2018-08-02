package io.openfuture.chain.core.service.block

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.repository.BlockRepository
import io.openfuture.chain.core.service.*
import io.openfuture.chain.network.component.node.NodeClock
import io.openfuture.chain.network.domain.NetworkGenesisBlock
import io.openfuture.chain.network.domain.NetworkMainBlock
import org.bouncycastle.asn1.x509.ObjectDigestInfo.publicKey
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultGenesisBlockService(
    private val repository: BlockRepository<GenesisBlock>,
    private val blockService: BlockService,
    private val delegateService: DefaultDelegateService,
    private val clock: NodeClock,
    private val consensusProperties: ConsensusProperties,
    private val keyHolder: NodeKeyHolder
    ) : GenesisBlockService {

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

    override fun add(dto: NetworkGenesisBlock) {
        if (!isValid(dto)) {
            return
        }

        val block = repository.findOneByHash(dto.hash!!)
        if (null != block) {
            return
        }

        val persistBlock = repository.save(GenesisBlock.of(dto))
        // todo broadcast
    }

    override fun isValid(block: NetworkGenesisBlock): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    @Transactional(readOnly = true)
    override fun getLast(): GenesisBlock = repository.findFirstByOrderByHeightDesc()
        ?: throw NotFoundException("Last block not found")


    @Transactional(readOnly = true)
    override fun isValid(block: GenesisBlock): Boolean {
        val lastBlock = getLast()
        val blockFound = commonBlockService.get(block.hash) as? GenesisBlock

        return (blockFound != null
            && isValidEpochIndex(lastBlock, block)
            && isValidateActiveDelegates(block))
    }

    private fun isValidEpochIndex(lastBlock: GenesisBlock, block: GenesisBlock): Boolean =
        (lastBlock.epochIndex + 1 == block.epochIndex)

    private fun isValidateActiveDelegates(block: GenesisBlock): Boolean {
        val activeDelegates = block.activeDelegates
        return activeDelegates == delegateService.getActiveDelegates()
    }

}