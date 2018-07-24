package io.openfuture.chain.service.transaction

import io.openfuture.chain.entity.transaction.BaseTransaction
import io.openfuture.chain.repository.BaseTransactionRepository
import io.openfuture.chain.service.CommonTransactionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultCommonTransactionService(
    protected val repository: BaseTransactionRepository<BaseTransaction>
) : CommonTransactionService {

    @Transactional(readOnly = true)
    override fun getAllPending(): MutableSet<BaseTransaction> {
        return repository.findAllByBlockIsNull()
    }

}