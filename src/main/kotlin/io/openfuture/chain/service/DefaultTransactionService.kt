package io.openfuture.chain.service

import io.openfuture.chain.entity.transaction.Transaction
import io.openfuture.chain.repository.TransactionRepository
import org.springframework.stereotype.Service

@Service
class DefaultTransactionService(
        repository: TransactionRepository<Transaction>
) : BaseTransactionService<Transaction>(repository), TransactionService<Transaction>