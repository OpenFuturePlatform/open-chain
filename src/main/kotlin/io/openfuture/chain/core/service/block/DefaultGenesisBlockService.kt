package io.openfuture.chain.core.service.block

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.annotation.BlockchainSynchronized
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.payload.GenesisBlockPayload
import io.openfuture.chain.core.repository.GenesisBlockRepository
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.DefaultDelegateService
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.core.service.WalletService
import io.openfuture.chain.core.sync.BlockchainLock
import io.openfuture.chain.core.sync.CurrentGenesisBlock
import io.openfuture.chain.core.sync.SyncManager
import io.openfuture.chain.crypto.util.SignatureUtils
import io.openfuture.chain.network.message.sync.GenesisBlockMessage
import io.openfuture.chain.rpc.domain.base.PageRequest
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultGenesisBlockService(
    blockService: BlockService,
    walletService: WalletService,
    delegateService: DefaultDelegateService,
    private val keyHolder: NodeKeyHolder,
    private val syncManager: SyncManager,
    private val consensusProperties: ConsensusProperties,
    private val currentGenesisBlock: CurrentGenesisBlock,
    private val genesisBlockRepository: GenesisBlockRepository
) : BaseBlockService<GenesisBlock>(genesisBlockRepository, blockService, walletService, delegateService), GenesisBlockService {

    @Transactional(readOnly = true)
    override fun getPreviousByHeight(height: Long): GenesisBlock = repository.findFirstByHeightLessThanOrderByHeightDesc(height)
        ?: throw NotFoundException("Previous block by height $height not found")

    @Transactional(readOnly = true)
    override fun getByHash(hash: String): GenesisBlock = repository.findOneByHash(hash)
        ?: throw NotFoundException("Block $hash not found")

    @Transactional(readOnly = true)
    override fun getNextBlock(hash: String): GenesisBlock {
        val block = getByHash(hash)

        return repository.findFirstByHeightGreaterThan(block.height)
            ?: throw NotFoundException("Next block by hash $hash not found")
    }

    @Transactional(readOnly = true)
    override fun getPreviousBlock(hash: String): GenesisBlock {
        val block = getByHash(hash)

        return getPreviousByHeight(block.height)
    }

    @Transactional(readOnly = true)
    override fun getAll(request: PageRequest): Page<GenesisBlock> = repository.findAll(request)

    override fun getLast(): GenesisBlock = currentGenesisBlock.block

    @Transactional(readOnly = true)
    override fun getByEpochIndex(epochIndex: Long): GenesisBlock? =
        genesisBlockRepository.findOneByPayloadEpochIndex(epochIndex)

    @BlockchainSynchronized
    @Transactional
    override fun create(): GenesisBlockMessage {
        val lastBlock = blockService.getLast()
        val timestamp = getTimestamp()
        val height = lastBlock.height + 1
        val previousHash = lastBlock.hash
        val payload = createPayload()
        val hash = createHash(timestamp, height, previousHash, payload)
        val signature = SignatureUtils.sign(hash, keyHolder.getPrivateKey())
        val publicKey = keyHolder.getPublicKeyAsHexString()

        return GenesisBlockMessage(height, previousHash, timestamp, ByteUtils.toHexString(hash), signature,
            publicKey, payload.epochIndex, payload.activeDelegates.map { it.publicKey })
    }

    @Transactional
    @Synchronized
    override fun add(message: GenesisBlockMessage) {
        if (null != repository.findOneByHash(message.hash)) {
            return
        }

        val delegates = message.delegates.asSequence().map { delegateService.getByPublicKey(it) }.toMutableList()
        val block = GenesisBlock.of(message, delegates)

        if (!isSync(block)) {
            syncManager.outOfSync(message.publicKey)
            return
        }

        super.save(block)
        currentGenesisBlock.block = block
    }

    override fun isGenesisBlockRequired(): Boolean {
        BlockchainLock.readLock.lock()
        try {
            val blocksProduced = blockService.getCurrentHeight() - getLast().height
            return (consensusProperties.epochHeight!!) <= blocksProduced
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

    private fun getTimestamp(): Long {
        val lastBlock = blockService.getLast()
        val nextTimeSlot = ((lastBlock.timestamp - getLast().timestamp) / consensusProperties.getPeriod()) + 1
        return getLast().timestamp + consensusProperties.getPeriod() * nextTimeSlot
    }

    private fun createPayload(): GenesisBlockPayload {
        val epochIndex = getLast().payload.epochIndex + 1
        val delegates = delegateService.getActiveDelegates().toMutableList()
        return GenesisBlockPayload(epochIndex, delegates)
    }

}