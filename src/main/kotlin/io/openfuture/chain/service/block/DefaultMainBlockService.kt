package io.openfuture.chain.service.block

import io.openfuture.chain.block.TimeSlot
import io.openfuture.chain.entity.block.MainBlock
import io.openfuture.chain.entity.transaction.*
import io.openfuture.chain.network.component.node.NodeClock
import io.openfuture.chain.network.domain.application.block.MainBlockMessage
import io.openfuture.chain.repository.MainBlockRepository
import io.openfuture.chain.service.*
import io.openfuture.chain.util.BlockUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultMainBlockService(
    repository: MainBlockRepository,
    private val clock: NodeClock,
    private val timeSlot: TimeSlot,
    private val voteTransactionService: VoteTransactionService,
    private val transferTransactionService: TransferTransactionService,
    private val delegateTransactionService: DelegateTransactionService,
    private val rewardTransactionService: RewardTransactionService
) : DefaultBlockService<MainBlock>(repository), MainBlockService {


    @Transactional
    override fun add(dto: MainBlockMessage) {
        val savedBlock = repository.save(dto.toEntity())

        dto.transferTransactions.forEach { transferTransactionService.toBlock(it, savedBlock) }
        dto.voteTransactions.forEach { voteTransactionService.toBlock(it, savedBlock) }
        dto.delegateTransactions.forEach { delegateTransactionService.toBlock(it, savedBlock) }
        dto.rewardTransactions.forEach { rewardTransactionService.toBlock(it, savedBlock) }
    }

    @Transactional
    override fun save(block: MainBlock): MainBlock {
        val savedBlock = super.save(block)
        val transactions = block.transactions
        for (transaction in transactions) {
            addTransactionToBlock(transaction, savedBlock)
        }
        return savedBlock
    }

    override fun isValid(block: MainBlock): Boolean {
        val currentTime = clock.networkTime()

        return timeSlot.verifyTimeSlot(currentTime, block) && isTransactionsValid(block)
    }

    private fun isTransactionsValid(block: MainBlock): Boolean {
        val transactions = block.transactions

        if (transactions.isEmpty() || !transactionsIsWellFormed(transactions)) {
            return false
        }

        val transactionsMerkleHash = BlockUtils.calculateMerkleRoot(transactions)
        return block.merkleHash == transactionsMerkleHash
    }

    private fun transactionsIsWellFormed(transactions: Set<Transaction>): Boolean {
        val transactionHashes = transactions.map { it.hash }.toSet()
        return transactionHashes.size == transactions.size
    }

    private fun addTransactionToBlock(tx: Transaction, block: MainBlock) {
        when (tx) {
            is VoteTransaction -> voteTransactionService.toBlock(tx.hash, block)
            is TransferTransaction -> transferTransactionService.toBlock(tx.hash, block)
            is DelegateTransaction -> delegateTransactionService.toBlock(tx.hash, block)
            is RewardTransaction -> rewardTransactionService.toBlock(tx, block)
            else -> throw IllegalStateException("Unknown transaction type")
        }
    }

}