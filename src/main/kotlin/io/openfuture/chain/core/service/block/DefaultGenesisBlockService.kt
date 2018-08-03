package io.openfuture.chain.core.service.block

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.block.BaseBlock
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.payload.GenesisBlockPayload
import io.openfuture.chain.core.repository.BlockRepository
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.DefaultDelegateService
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.core.util.BlockUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import io.openfuture.chain.network.component.node.NodeClock
import io.openfuture.chain.network.message.core.GenesisBlockMessage
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

    override fun create(): GenesisBlock {
        val timestamp = clock.networkTime()
        val lastBlock = blockService.getLast()
        val height = lastBlock.height + 1
        val payload = createPayload(lastBlock)
        val signature = SignatureUtils.sign(payload.getBytes(), keyHolder.getPrivateKey())
        val hash = BlockUtils.createHash(payload, keyHolder.getPublicKey(), signature)
        val publicKey = ByteUtils.toHexString(keyHolder.getPublicKey())

        return GenesisBlock(timestamp, height, hash, signature, publicKey, payload)
    }

    private fun createPayload(lastBlock: BaseBlock): GenesisBlockPayload {
        val previousHash = lastBlock.hash
        val reward = consensusProperties.rewardBlock!!
        val epochIndex = getLast().getPayload().epochIndex + 1
        val delegates = delegateService.getActiveDelegates()
        return GenesisBlockPayload(previousHash, reward, epochIndex, delegates)
    }

    @Transactional
    override fun add(message: GenesisBlockMessage) {
        if (!isValid(message)) {
            return
        }

        val block = repository.findOneByHash(message.hash)
        if (null != block) {
            return
        }

        repository.save(GenesisBlock.of(message))
        // todo broadcast
    }

    @Transactional
    override fun isValid(message: GenesisBlockMessage): Boolean {
        val block = GenesisBlock.of(message)
        val delegates = message.delegates.map { delegateService.getByPublicKey(it) }

        val lastGenesisBlock = this.getLast()
        return super.isValid(GenesisBlock.of(message))
            && isValidateActiveDelegates(delegates)
            && isValidEpochIndex(lastGenesisBlock, block)
    }

    private fun isValidateActiveDelegates(delegates: List<Delegate>): Boolean {
        val persistDelegates = delegateService.getActiveDelegates()
        if (delegates.size != persistDelegates.size) {
            return false
        }

        val persistPublicKeys = persistDelegates.map { it.publicKey }
        val publicKeys = delegates.map { it.publicKey }

        return persistPublicKeys.containsAll(publicKeys)
    }

    private fun isValidEpochIndex(lastBlock: GenesisBlock, block: GenesisBlock): Boolean {
        return (lastBlock.getPayload().epochIndex + 1 == block.getPayload().epochIndex)
    }

}