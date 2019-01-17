package io.openfuture.chain.core.service.block

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.repository.*
import io.openfuture.chain.core.service.BlockService
import io.openfuture.chain.core.service.TransactionService
import io.openfuture.chain.core.service.TransferTransactionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultBlockService(
    private val repository: BlockRepository<Block>,
    private val properties: ConsensusProperties,
    private val transferTransactionRepository: TransferTransactionRepository,
    private val voteTransactionRepository: VoteTransactionRepository,
    private val delegateTransactionRepository: DelegateTransactionRepository,
    private val rewardTransactionRepository: RewardTransactionRepository
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
        val blocks = repository.findAllByHeightBetween(fromHeight, toHeight)
        for (block in blocks) {
            if (block is MainBlock) {
                val payload = block.payload

                transferTransactionRepository.deleteAll(payload.transferTransactions)
                delegateTransactionRepository.deleteAll(payload.delegateTransactions)
                voteTransactionRepository.deleteAll(payload.voteTransactions)
                rewardTransactionRepository.deleteAll(payload.rewardTransaction)
            }
            val blockToRemove = repository.findOneByHash(block.hash)!!
            repository.delete(blockToRemove)
        }
    }

    @Transactional(readOnly = true)
    override fun isExists(hash: String): Boolean = repository.findOneByHash(hash)?.let { true } ?: false

    @Transactional(readOnly = true)
    override fun findByHash(hash: String): Block?  = repository.findOneByHash(hash)

    @Transactional(readOnly = true)
    override fun isExists(hash: String, height: Long): Boolean =
        repository.findOneByHashAndHeight(hash, height)?.let { true } ?: false

    @Transactional(readOnly = true)
    override fun getCurrentHeight(): Long = repository.getCurrentHeight()

}