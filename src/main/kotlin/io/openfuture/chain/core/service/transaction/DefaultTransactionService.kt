package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.core.model.entity.transaction.confirmed.Transaction
import io.openfuture.chain.core.repository.TransactionRepository
import io.openfuture.chain.core.service.TransactionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultTransactionService(
    private val repository: TransactionRepository<Transaction>
) : TransactionService {

    @Transactional(readOnly = true)
    override fun getCount(): Long {
        return repository.count()
    }

}