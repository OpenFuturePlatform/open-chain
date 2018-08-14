package io.openfuture.chain.core.service.block

import io.openfuture.chain.core.component.BlockCapacityChecker
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.block.payload.GenesisBlockPayload
import io.openfuture.chain.core.repository.GenesisBlockRepository
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.DefaultDelegateService
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.core.service.WalletService
import io.openfuture.chain.core.util.BlockUtils
import io.openfuture.chain.crypto.util.SignatureUtils
import io.openfuture.chain.network.message.core.GenesisBlockMessage
import io.openfuture.chain.network.service.NetworkApiService
import io.openfuture.chain.rpc.domain.base.PageRequest
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.data.domain.Page
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultGenesisBlockService(
    blockService: BlockService,
    repository: GenesisBlockRepository,
    walletService: WalletService,
    delegateService: DefaultDelegateService,
    capacityChecker: BlockCapacityChecker,
    private val keyHolder: NodeKeyHolder,
    private val networkService: NetworkApiService
) : BaseBlockService<GenesisBlock>(repository, blockService, walletService, delegateService, capacityChecker), GenesisBlockService {

    @Transactional(readOnly = true)
    override fun getAll(request: PageRequest): Page<GenesisBlock> = repository.findAll(request)

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

        val block = GenesisBlock(timestamp, height, previousHash, reward, ByteUtils.toHexString(hash), signature,
            publicKey, payload)
        return GenesisBlockMessage(block)
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

        if (!isValid(block)) {
            return
        }

        super.save(block)
        networkService.broadcast(message)
    }

    @Transactional(readOnly = true)
    override fun isValid(message: GenesisBlockMessage): Boolean {
        val delegates = message.delegates.map { delegateService.getByPublicKey(it) }
        val block = GenesisBlock.of(message, delegates)
        return isValid(block)
    }

    private fun isValid(block: GenesisBlock): Boolean {
        return isValidateActiveDelegates(block.payload.activeDelegates)
            && isValidEpochIndex(this.getLast().payload.epochIndex, block.payload.epochIndex)
            && super.isValid(block)
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