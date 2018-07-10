package io.openfuture.chain.service

import io.openfuture.chain.entity.Transaction
import io.openfuture.chain.events.BlockCreationEvent
import io.openfuture.chain.repository.TransactionRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service

@Service
class DefaultTransactionService(
    private val transactionRepository: TransactionRepository,
    private val eventPublisher: ApplicationEventPublisher
): TransactionService {

    override fun save(transaction: Transaction): Transaction {
        val savedTransaction = transactionRepository.save(transaction)
        eventPublisher.publishEvent(BlockCreationEvent())
        return savedTransaction
    }

    override fun getPendingTransactions(): List<Transaction> {
        return transactionRepository.findAllByBlockIdIsNull()
    }

}