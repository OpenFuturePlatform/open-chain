package io.openfuture.chain.core.service.block

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.annotation.BlockchainSynchronized
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.exception.ValidationException
import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.payload.GenesisBlockPayload
import io.openfuture.chain.core.repository.GenesisBlockRepository
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.DefaultDelegateService
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.core.service.WalletService
import io.openfuture.chain.core.sync.SyncStatus
import io.openfuture.chain.core.sync.SyncStatus.SyncStatusType.NOT_SYNCHRONIZED
import io.openfuture.chain.crypto.util.SignatureUtils
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
    repository: GenesisBlockRepository,
    private val keyHolder: NodeKeyHolder,
    private val syncStatus: SyncStatus,
    private val consensusProperties: ConsensusProperties
) : BaseBlockService<GenesisBlock>(repository, blockService, walletService, delegateService), GenesisBlockService {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(DefaultGenesisBlockService::class.java)
    }

    @Volatile private var last: GenesisBlock = repository.findFirstByOrderByHeightDesc()!!

    @Transactional(readOnly = true)
    override fun getPreviousByHeight(height: Long): GenesisBlock = repository.findFirstByHeightLessThanOrderByHeightDesc(height)
        ?: throw NotFoundException("Previous block by height $height not found")

    @Transactional(readOnly = true)
    override fun getByHash(hash: String): GenesisBlock = repository.findOneByHash(hash)
        ?: throw NotFoundException("Block by $hash not found")

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

    @Transactional(readOnly = true)
    override fun getLast(): GenesisBlock = last

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
    override fun add(message: GenesisBlockMessage) {
        if (null != repository.findOneByHash(message.hash)) {
            return
        }

        val delegates = message.delegates.map { delegateService.getByPublicKey(it) }
        val block = GenesisBlock.of(message, delegates)

        if (!isSync(block)) {
            syncStatus.setSyncStatus(NOT_SYNCHRONIZED)
            return
        }

        super.save(block)
        last = block
    }

    @Transactional(readOnly = true)
    override fun verify(message: GenesisBlockMessage): Boolean {
        return try {
            validate(message)
            true
        } catch (e: ValidationException) {
            log.warn(e.message)
            false
        }
    }

    override fun isGenesisBlockRequired(): Boolean {
        val blocksProduced = blockService.getCurrentHeight() - getLast().height
        return (consensusProperties.epochHeight!! - 1) <= blocksProduced
    }

    private fun getTimestamp(): Long {
        val lastBlock = blockService.getLast()
        val nextTimeSlot = ((lastBlock.timestamp - getLast().timestamp) / consensusProperties.getPeriod()) + 1
        return getLast().timestamp + consensusProperties.getPeriod() * nextTimeSlot
    }

    private fun createPayload(): GenesisBlockPayload {
        val epochIndex = getLast().payload.epochIndex + 1
        val delegates = delegateService.getActiveDelegates()
        return GenesisBlockPayload(epochIndex, delegates)
    }

    private fun validate(message: GenesisBlockMessage) {
        val delegates = message.delegates.map { delegateService.getByPublicKey(it) }
        val block = GenesisBlock.of(message, delegates)

        if (!isValidateActiveDelegates(block.payload.activeDelegates)) {
            throw ValidationException("Invalid active delegates")
        }

        if (!isValidEpochIndex(this.getLast().payload.epochIndex, block.payload.epochIndex)) {
            throw ValidationException("Invalid epoch index: ${block.payload.epochIndex}")
        }

        super.validateBase(block)
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

    private fun isValidEpochIndex(lastEpoch: Long, newEpoch: Long): Boolean = lastEpoch + 1 == newEpoch

}