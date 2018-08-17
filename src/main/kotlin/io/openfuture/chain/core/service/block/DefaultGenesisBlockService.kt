package io.openfuture.chain.core.service.block

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
import io.openfuture.chain.core.util.BlockUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import io.openfuture.chain.network.message.core.GenesisBlockMessage
import io.openfuture.chain.network.sync.SyncManager
import io.openfuture.chain.network.sync.impl.SynchronizationStatus.NOT_SYNCHRONIZED
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultGenesisBlockService(
    blockService: BlockService,
    repository: GenesisBlockRepository,
    walletService: WalletService,
    delegateService: DefaultDelegateService,
    private val keyHolder: NodeKeyHolder,
    private val syncManager: SyncManager
) : BaseBlockService<GenesisBlock>(repository, blockService, walletService, delegateService), GenesisBlockService {

    companion object {
        val log = LoggerFactory.getLogger(DefaultGenesisBlockService::class.java)
    }


    @Transactional(readOnly = true)
    override fun getLast(): GenesisBlock = repository.findFirstByOrderByHeightDesc()
        ?: throw NotFoundException("Last block not found")

    override fun create(timestamp: Long): GenesisBlockMessage {
        val lastBlock = blockService.getLast()
        val height = lastBlock.height + 1
        val previousHash = lastBlock.hash
        val reward = 0L
        val payload = createPayload()
        val hash = BlockUtils.createHash(timestamp, height, previousHash, reward, payload)
        val signature = SignatureUtils.sign(hash, keyHolder.getPrivateKey())
        val publicKey = keyHolder.getPublicKey()

        return GenesisBlockMessage(height, previousHash, timestamp, reward, ByteUtils.toHexString(hash), signature,
            publicKey, payload.epochIndex, payload.activeDelegates.map { it.publicKey })
    }

    private fun createPayload(): GenesisBlockPayload {
        val epochIndex = getLast().payload.epochIndex + 1
        val delegates = delegateService.getActiveDelegates()
        return GenesisBlockPayload(epochIndex, delegates)
    }

    @Transactional
    override fun add(message: GenesisBlockMessage) {
        if (null != repository.findOneByHash(message.hash)) {
            return
        }

        val delegates = message.delegates.map { delegateService.getByPublicKey(it) }
        val block = GenesisBlock.of(message, delegates)

        if (!isSync(block)) {
            syncManager.setSyncStatus(NOT_SYNCHRONIZED)
            return
        }

        super.save(block)
    }

    @Transactional(readOnly = true)
    override fun isValid(message: GenesisBlockMessage): Boolean {
        return try {
            validate(message)
            true
        } catch (e: ValidationException) {
            log.warn(e.message)
            false
        }
    }

    private fun validate(message: GenesisBlockMessage) {
        val delegates = message.delegates.map { delegateService.getByPublicKey(it) }
        val block = GenesisBlock.of(message, delegates)

        validateLocal(block)
        super.validateBase(block)
    }

    private fun validateLocal(block: GenesisBlock) {
        if (!isValidateActiveDelegates(block.payload.activeDelegates)) {
            throw ValidationException("Invalid active delegates")
        }

        if (!isValidEpochIndex(this.getLast().payload.epochIndex, block.payload.epochIndex)) {
            throw ValidationException("Invalid epoch index: ${block.payload.epochIndex}")
        }
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