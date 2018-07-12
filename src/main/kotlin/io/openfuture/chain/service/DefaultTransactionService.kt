package io.openfuture.chain.service

import io.openfuture.chain.entity.Transaction
import io.openfuture.chain.events.BlockCreationEvent
import io.openfuture.chain.repository.TransactionRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultTransactionService(
    private val transactionRepository: TransactionRepository,
    private val eventPublisher: ApplicationEventPublisher,
    @Value("\${block.capacity}") private val transactionCapacity: Int
): TransactionService {

    @Transactional
    override fun save(transaction: Transaction): Transaction {
        val savedTransaction = transactionRepository.save(transaction)
        val pendingTransactions = getPendingTransactions()
        if (pendingTransactions.size >= transactionCapacity) {
            eventPublisher.publishEvent(BlockCreationEvent(pendingTransactions))
        }
        return savedTransaction
    }

    @Transactional
    override fun saveAll(transactions: List<Transaction>): List<Transaction>
        = transactionRepository.saveAll(transactions)

    @Transactional(readOnly = true)
    override fun getPendingTransactions(): List<Transaction>
        = transactionRepository.findAllByBlockHashIsNull()


}