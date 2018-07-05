package io.openfuture.chain.service

import io.openfuture.chain.entity.Transaction
import io.openfuture.chain.repository.TransactionRepository
import org.springframework.stereotype.Service

@Service
class DefaultTransactionService(
        private val repository: TransactionRepository
) : BaseTransactionService<Transaction>(repository)