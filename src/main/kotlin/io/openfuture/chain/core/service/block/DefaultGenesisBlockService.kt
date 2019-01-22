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
import io.openfuture.chain.crypto.util.SignatureUtils
import io.openfuture.chain.network.handler.sync.EpochResponseHandler
import io.openfuture.chain.network.message.sync.GenesisBlockMessage
import io.openfuture.chain.rpc.domain.base.PageRequest
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultGenesisBlockService(
    blockService: BlockService,
    walletService: WalletService,
    delegateService: DefaultDelegateService,
    private val keyHolder: NodeKeyHolder,
    private val consensusProperties: ConsensusProperties,
    private val genesisBlockRepository: GenesisBlockRepository
) : BaseBlockService<GenesisBlock>(genesisBlockRepository, blockService, walletService, delegateService), GenesisBlockService {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(EpochResponseHandler::class.java)
    }

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

    override fun getLast(): GenesisBlock = genesisBlockRepository.findFirstByOrderByHeightDesc()!!

    @Transactional(readOnly = true)
    override fun getByEpochIndex(epochIndex: Long): GenesisBlock? =
        genesisBlockRepository.findOneByPayloadEpochIndex(epochIndex)

    @BlockchainSynchronized
    @Transactional
    override fun create(): GenesisBlock {
        val lastBlock = blockService.getLast()
        val timestamp = getTimestamp()
        val height = lastBlock.height + 1
        val previousHash = lastBlock.hash
        val payload = createPayload()
        val hash = createHash(timestamp, height, previousHash, payload)
        val signature = SignatureUtils.sign(hash, keyHolder.getPrivateKey())
        val publicKey = keyHolder.getPublicKeyAsHexString()

        return GenesisBlock(timestamp, height, previousHash, ByteUtils.toHexString(hash), signature, publicKey, payload)
    }

    @Transactional
    @Synchronized
    override fun add(block: GenesisBlock) {
        super.save(block)
    }

    @Transactional
    @Synchronized
    override fun add(message: GenesisBlockMessage) {
        if (null != repository.findOneByHash(message.hash)) {
            return
        }

        val block = GenesisBlock.of(message)

        add(block)
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
        val firstGenesisBlock = genesisBlockRepository.findOneByPayloadEpochIndex(1)!!
        val genesisDelegates = firstGenesisBlock.payload.activeDelegates
        val epochIndex = getLast().payload.epochIndex + 1
        val delegates = delegateService.getActiveDelegates().toMutableSet()
        delegates.addAll(genesisDelegates)
        return GenesisBlockPayload(epochIndex, delegates.toMutableList())
    }

}