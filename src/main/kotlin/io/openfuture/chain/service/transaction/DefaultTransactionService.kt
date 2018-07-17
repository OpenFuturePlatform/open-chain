package io.openfuture.chain.service.transaction

import io.openfuture.chain.entity.transaction.BaseTransaction
import io.openfuture.chain.repository.BaseTransactionRepository
import io.openfuture.chain.service.BaseTransactionService
import org.springframework.stereotype.Service

@Service
class DefaultTransactionService(
    repository: BaseTransactionRepository<BaseTransaction>
) : DefaultBaseTransactionService<BaseTransaction>(repository), BaseTransactionService<BaseTransaction>