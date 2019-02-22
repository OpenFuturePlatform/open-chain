package io.openfuture.chain.core.service.block

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.annotation.BlockchainSynchronized
import io.openfuture.chain.core.component.NodeKeyHolder
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.payload.GenesisBlockPayload
import io.openfuture.chain.core.repository.GenesisBlockRepository
import io.openfuture.chain.core.service.GenesisBlockService
import io.openfuture.chain.core.service.StateManager
import io.openfuture.chain.core.sync.BlockchainLock
import io.openfuture.chain.crypto.util.SignatureUtils
import io.openfuture.chain.network.handler.sync.EpochResponseHandler
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultGenesisBlockService(
    private val repository: GenesisBlockRepository,
    private val stateManager: StateManager,
    private val keyHolder: NodeKeyHolder,
    private val consensusProperties: ConsensusProperties
) : DefaultBlockService<GenesisBlock>(repository), GenesisBlockService {

    companion object {
        private val log: Logger = LoggerFactory.getLogger(EpochResponseHandler::class.java)
    }


    override fun getPreviousByHeight(height: Long): GenesisBlock = repository.findFirstByHeightLessThanOrderByHeightDesc(height)
        ?: throw NotFoundException("Previous block by height $height not found")

    override fun findByEpochIndex(epochIndex: Long): GenesisBlock? =
        repository.findOneByPayloadEpochIndex(epochIndex)

    override fun isGenesisBlockRequired(): Boolean {
        BlockchainLock.readLock.lock()
        try {
            val blocksProduced = blockRepository.getCurrentHeight() - getLast().height
            return (consensusProperties.epochHeight!!) == blocksProduced.toInt()
        } finally {
            BlockchainLock.readLock.unlock()
        }
    }

    @BlockchainSynchronized
    @Transactional
    override fun create(): GenesisBlock {
        val last = getLast()
        val lastBlock = getLastBlock()
        val timestamp = getTimestamp(last, lastBlock)
        val height = lastBlock.height + 1
        val previousHash = lastBlock.hash
        val payload = createPayload(last)
        val hash = Block.generateHash(timestamp, height, previousHash, payload)
        val signature = SignatureUtils.sign(ByteUtils.fromHexString(hash), keyHolder.getPrivateKey())
        val publicKey = keyHolder.getPublicKeyAsHexString()

        return GenesisBlock(timestamp, height, previousHash, hash, signature, publicKey, payload)
    }

    @Transactional
    @Synchronized
    override fun add(block: GenesisBlock) {
        if (null != repository.findOneByHash(block.hash)) {
            return
        }

        repository.save(block)
        log.debug("Saving genesis block: height #${block.height}, hash ${block.hash}")
    }

    private fun getTimestamp(last: GenesisBlock, lastBlock: Block): Long {
        val nextTimeSlot = ((lastBlock.timestamp - last.timestamp) / consensusProperties.getPeriod()) + 1
        return last.timestamp + consensusProperties.getPeriod() * nextTimeSlot
    }

    private fun createPayload(last: GenesisBlock): GenesisBlockPayload {
        val firstGenesisBlock = repository.findOneByPayloadEpochIndex(1)!!
        val genesisDelegates = firstGenesisBlock.getPayload().activeDelegates
        val epochIndex = last.getPayload().epochIndex + 1
        val delegates = stateManager.getActiveDelegates().map { it.address }.toMutableSet()
        delegates.addAll(genesisDelegates)
        return GenesisBlockPayload(epochIndex, delegates.toList())
    }

}