package io.openfuture.chain.service

import io.openfuture.chain.entity.Transaction
import io.openfuture.chain.repository.TransactionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultTransactionService(
    private val repository: TransactionRepository
) : TransactionService {

    @Transactional
    override fun save(transaction: Transaction): Transaction {
        return repository.save(transaction)
    }

}