package io.openfuture.chain.service.transaction

import io.openfuture.chain.entity.transaction.BaseTransaction
import io.openfuture.chain.repository.BaseTransactionRepository
import io.openfuture.chain.service.BaseTransactionService
import org.springframework.data.querydsl.QPageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultBaseTransactionService(
    private val repository: BaseTransactionRepository<BaseTransaction>
) : BaseTransactionService {

    @Transactional(readOnly = true)
    override fun getAllPending(): MutableSet<BaseTransaction> {
        return repository.findAllByBlockIsNull()
    }

    @Transactional(readOnly = true)
    override fun getAllPending(limit: Int) : MutableSet<BaseTransaction> {
        val pageable = QPageRequest(0, limit)
        return HashSet(repository.findAll(pageable).content)
    }

}