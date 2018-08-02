package io.openfuture.chain.core.service.block

import io.openfuture.chain.consensus.model.entity.transaction.DelegateTransaction
import io.openfuture.chain.consensus.model.entity.transaction.TransferTransaction
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.Transaction
import io.openfuture.chain.core.repository.MainBlockRepository
import io.openfuture.chain.core.service.DelegateTransactionService
import io.openfuture.chain.core.service.MainBlockService
import io.openfuture.chain.core.service.TransferTransactionService
import io.openfuture.chain.core.service.VoteTransactionService
import io.openfuture.chain.core.util.TransactionUtils
import io.openfuture.chain.entity.transaction.VoteTransaction
import io.openfuture.chain.network.domain.NetworkMainBlock
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultMainBlockService(
    private val repository: MainBlockRepository,
    private val voteTransactionService: VoteTransactionService,
    private val transferTransactionService: TransferTransactionService,
    private val delegateTransactionService: DelegateTransactionService
) : MainBlockService {

    @Transactional(readOnly = true)
    override fun getLast(): MainBlock = repository.findFirstByOrderByHeightDesc()
        ?: throw NotFoundException("Last block not found")

    @Transactional
    override fun add(dto: NetworkMainBlock) {
        val savedBlock = repository.save(dto.toEntity())

        dto.transferTransactions.forEach { transferTransactionService.toBlock(it, savedBlock) }
        dto.voteTransactions.forEach { voteTransactionService.toBlock(it, savedBlock) }
        dto.delegateTransactions.forEach { delegateTransactionService.toBlock(it, savedBlock) }
    }

    @Transactional
    override fun save(block: MainBlock): MainBlock {
        val savedBlock = repository.save(block)
        val transactions = block.transactions
        for (transaction in transactions) {
            addTransactionToBlock(transaction, savedBlock)
        }
        return savedBlock
    }

    override fun isValid(block: MainBlock): Boolean {
        return true
    }

    private fun isTransactionsValid(block: MainBlock): Boolean {
        val transactions = block.transactions

        if (transactions.isEmpty() || !transactionsIsWellFormed(transactions)) {
            return false
        }

        val transactionsMerkleHash = TransactionUtils.calculateMerkleRoot(transactions)
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
            else -> throw IllegalStateException("Unknown transaction type")
        }
    }

}