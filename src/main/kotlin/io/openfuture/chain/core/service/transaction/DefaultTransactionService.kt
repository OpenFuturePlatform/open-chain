package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.core.model.entity.transaction.confirmed.Transaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UTransaction
import io.openfuture.chain.core.repository.TransactionRepository
import io.openfuture.chain.core.repository.UTransactionRepository
import io.openfuture.chain.core.service.TransactionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultTransactionService(
    private val repository: TransactionRepository<Transaction>,
    private val unconfirmedRepository: UTransactionRepository<UTransaction>
) : TransactionService {

    @Transactional(readOnly = true)
    override fun getCount(): Long {
        return repository.count()
    }

}