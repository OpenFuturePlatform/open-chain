package io.openfuture.chain.core.service.block

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.Receipt
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.block.payload.BlockPayload
import io.openfuture.chain.core.model.entity.state.State
import io.openfuture.chain.core.model.entity.transaction.confirmed.Transaction
import io.openfuture.chain.core.repository.BlockRepository
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.ReceiptService
import io.openfuture.chain.core.service.StateManager
import io.openfuture.chain.core.service.TransactionManager
import io.openfuture.chain.core.sync.SyncMode
import io.openfuture.chain.core.sync.SyncMode.FULL
import io.openfuture.chain.core.util.ByteConstants.LONG_BYTES
import io.openfuture.chain.crypto.util.HashUtils
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.ByteBuffer

@Service
class DefaultBlockService(
    private val repository: BlockRepository<Block>,
    private val properties: ConsensusProperties,
    private val transactionManager: TransactionManager,
    private val stateManager: StateManager,
    private val receiptService: ReceiptService
) : BlockService {

    @Transactional(readOnly = true)
    override fun getAfterCurrentHash(hash: String): List<Block> {
        val startBlock = repository.findOneByHash(hash) ?: return emptyList()
        return repository.findAllByHeightGreaterThan(startBlock.height)
    }

    override fun getAvgProductionTime(): Long {
        val lastBlock = getLast()
        val firstMainBlock = repository.findFirstByHeightGreaterThan(1) ?: return 0
        return (lastBlock.timestamp - firstMainBlock.timestamp) / lastBlock.height
    }

    @Transactional(readOnly = true)
    override fun getCount(): Long = repository.count()

    @Transactional(readOnly = true)
    override fun getLast(): Block =
        repository.findFirstByOrderByHeightDesc() ?: throw NotFoundException("Last block not found!")

    @Transactional
    override fun save(block: Block) {
        repository.save(block)
    }

    @Transactional
    override fun removeEpoch(genesisBlock: GenesisBlock) {
        val fromHeight = if (1L == genesisBlock.height) {
            genesisBlock.height + 1
        } else {
            genesisBlock.height
        }
        val toHeight = fromHeight + properties.epochHeight!!
        val heightRange = (fromHeight..toHeight).toList()
        transactionManager.deleteBlockTransactions(heightRange)
        stateManager.deleteBlockStates(heightRange)
        receiptService.deleteBlockReceipts(heightRange)
        repository.deleteAllByHeightIn(heightRange)
    }

    @Transactional(readOnly = true)
    override fun isExists(hash: String): Boolean = repository.findOneByHash(hash)?.let { true } ?: false

    @Transactional(readOnly = true)
    override fun findByHash(hash: String): Block? = repository.findOneByHash(hash)

    @Transactional(readOnly = true)
    override fun isExists(hash: String, height: Long): Boolean =
        repository.findOneByHashAndHeight(hash, height)?.let { true } ?: false

    @Transactional(readOnly = true)
    override fun getAllByHeightIn(heights: List<Long>): List<Block> =
        repository.findAllByHeightIn(heights)


    @Transactional
    override fun deleteByHeightIn(heights: List<Long>) {
        stateManager.deleteBlockStates(heights)
        transactionManager.deleteBlockTransactions(heights)
        repository.deleteAllByHeightIn(heights)
    }

    override fun isValidHash(block: Block): Boolean {
        val hash = createHash(block.timestamp, block.height, block.previousHash, block.getPayload())
        return ByteUtils.toHexString(hash) == block.hash
    }

    override fun createHash(timestamp: Long, height: Long, previousHash: String, payload: BlockPayload): ByteArray {
        val bytes = ByteBuffer.allocate(LONG_BYTES + LONG_BYTES + previousHash.toByteArray().size + payload.getBytes().size)
            .putLong(timestamp)
            .putLong(height)
            .put(previousHash.toByteArray())
            .put(payload.getBytes())
            .array()

        return HashUtils.doubleSha256(bytes)
    }

    @Transactional(readOnly = true)
    override fun getCurrentHeight(): Long = repository.getCurrentHeight()

    @Transactional
    override fun saveChunk(blocksChunk: List<Block>, syncMode: SyncMode) {
        blocksChunk.forEach { block ->
            if (block is MainBlock) {
                val rewardTransaction = block.payload.rewardTransaction.first()
                block.payload.rewardTransaction = mutableListOf()

                val transactions = mutableListOf<Transaction>()
                val states = mutableListOf<State>()
                val receipts = mutableSetOf<Receipt>()

                if (syncMode == FULL) {
                    transactions.addAll(block.payload.transferTransactions)
                    transactions.addAll(block.payload.voteTransactions)
                    transactions.addAll(block.payload.delegateTransactions)

                    block.payload.transferTransactions = mutableListOf()
                    block.payload.voteTransactions = mutableListOf()
                    block.payload.delegateTransactions = mutableListOf()

                    receipts.addAll(block.payload.receipts)
                    block.payload.receipts = mutableListOf()
                }

                states.addAll(block.payload.delegateStates)
                states.addAll(block.payload.accountStates)

                block.payload.delegateStates = mutableListOf()
                block.payload.accountStates = mutableListOf()

                this.save(block)
                var receipt = receipts.find { receipt -> receipt.transactionHash == rewardTransaction.hash }!!
                transactionManager.commit(rewardTransaction, receipt)

                if (syncMode == FULL) {
                    receipts.forEach {
                        it.block = block
                        receiptService.commit(it)
                    }

                    transactions.forEach {
                        it.block = block
                        receipt = receipts.find { receipt -> receipt.transactionHash == it.hash }!!
                        transactionManager.commit(it, receipt)
                    }
                }

                states.forEach {
                    it.block = block
                    stateManager.commit(it)
                }
            } else if (block is GenesisBlock) {
                this.save(block)
            }
        }
    }

}