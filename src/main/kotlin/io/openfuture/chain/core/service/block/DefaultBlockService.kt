package io.openfuture.chain.core.service.block

import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.GenesisBlock
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.state.DelegateState
import io.openfuture.chain.core.model.entity.state.State
import io.openfuture.chain.core.model.entity.state.WalletState
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.Transaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import io.openfuture.chain.core.repository.BlockRepository
import io.openfuture.chain.core.service.*
import io.openfuture.chain.core.sync.SyncMode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultBlockService(
    private val repository: BlockRepository<Block>,
    private val voteTransactionService: VoteTransactionService,
    private val rewardTransactionService: RewardTransactionService,
    private val delegateTransactionService: DelegateTransactionService,
    private val transferTransactionService: TransferTransactionService,
    private val delegateStateService: DelegateStateService,
    private val walletStateService: WalletStateService
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

    @Transactional(readOnly = true)
    override fun isExists(hash: String): Boolean = repository.findOneByHash(hash)?.let { true } ?: false

    @Transactional(readOnly = true)
    override fun isExists(hash: String, height: Long): Boolean =
        repository.findOneByHashAndHeight(hash, height)?.let { true } ?: false

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

                if (syncMode == SyncMode.FULL) {
                    transactions.addAll(block.payload.transferTransactions)
                    transactions.addAll(block.payload.voteTransactions)
                    transactions.addAll(block.payload.delegateTransactions)

                    block.payload.transferTransactions = mutableListOf()
                    block.payload.voteTransactions = mutableListOf()
                    block.payload.delegateTransactions = mutableListOf()
                }

                states.addAll(block.payload.delegateStates)
                states.addAll(block.payload.walletStates)

                block.payload.delegateStates = mutableListOf()
                block.payload.walletStates = mutableListOf()

                this.save(block)
                rewardTransactionService.commit(rewardTransaction)

                if (syncMode == SyncMode.FULL) {
                    transactions.forEach {
                        it.block = block
                        when (it) {
                            is TransferTransaction -> transferTransactionService.commit(it)
                            is DelegateTransaction -> delegateTransactionService.commit(it)
                            is VoteTransaction -> voteTransactionService.commit(it)
                            else -> throw IllegalStateException("The type doesn`t handle")
                        }
                    }
                }
                states.forEach {
                    it.block = block
                    when (it) {
                        is DelegateState -> delegateStateService.commit(it)
                        is WalletState -> walletStateService.commit(it)
                        else -> throw IllegalStateException("The type doesn`t handle")
                    }
                }
            } else if (block is GenesisBlock) {
                this.save(block)
            }
        }
    }

}