package io.openfuture.chain.core.service.block

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.payload.GenesisBlockPayload
import io.openfuture.chain.core.repository.BlockRepository
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.DefaultDelegateService
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.core.util.BlockUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import io.openfuture.chain.network.component.node.NodeClock
import io.openfuture.chain.network.message.application.block.BlockMessage
import io.openfuture.chain.network.message.application.block.GenesisBlockMessage
import io.openfuture.chain.network.message.application.delegate.DelegateMessage
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

    override fun create(): GenesisBlockMessage {
        val timestamp = clock.networkTime()
        val lastBlock = blockService.getLast()
        val height = lastBlock.height + 1
        val payload = createPayload(lastBlock)
        val signature = SignatureUtils.sign(payload.getBytes(), keyHolder.getPrivateKey())
        val hash = BlockUtils.createHash(payload, keyHolder.getPublicKey(), signature)
        val publicKey = ByteUtils.toHexString(keyHolder.getPublicKey())
        val genesisBlock = GenesisBlock(timestamp, height, payload, hash, signature, publicKey)
        return GenesisBlockMessage(genesisBlock)
    }

    private fun createPayload(lastBlock: BlockMessage): GenesisBlockPayload {
        val previousHash = lastBlock.hash
        val reward = consensusProperties.rewardBlock!!
        val epochIndex = getLast().payload.epochIndex + 1
        val delegates = delegateService.getActiveDelegates().map { it.toMessage() }.toMutableSet()
        return GenesisBlockPayload(previousHash, reward, epochIndex, delegates)
    }

    @Transactional
    override fun add(dto: GenesisBlockMessage) {
        if (!isValid(dto)) {
            return
        }

        val block = repository.findOneByHash(dto.hash)
        if (null != block) {
            return
        }

        repository.save(GenesisBlock.of(dto))
        // todo broadcast
    }

    @Transactional
    override fun isValid(block: GenesisBlockMessage): Boolean {
        val lastGenesisBlock = this.getLast()
        return super.isValid(block)
            && !block.activeDelegates.isEmpty()
            && isValidateActiveDelegates(block.activeDelegates)
            && isValidEpochIndex(lastGenesisBlock.payload.epochIndex, block.epochIndex)
    }

    private fun isValidateActiveDelegates(activeDelegates: MutableSet<DelegateMessage>): Boolean {
        return activeDelegates == delegateService.getActiveDelegates().map { it.toMessage() }.toMutableSet()
    }

    private fun isValidEpochIndex(lastEpoch: Long, newEpoch: Long): Boolean {
        return (lastEpoch + 1 == newEpoch)
    }

}