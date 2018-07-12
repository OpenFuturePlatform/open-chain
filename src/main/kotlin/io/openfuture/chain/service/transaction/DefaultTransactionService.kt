package io.openfuture.chain.service.transaction

import io.openfuture.chain.entity.transaction.BaseTransaction
import io.openfuture.chain.repository.BaseTransactionRepository
import org.springframework.stereotype.Service

@Service
class DefaultTransactionService(
    repository: BaseTransactionRepository<BaseTransaction>
) : DefaultBaseTransactionService<BaseTransaction>(repository)