package io.openfuture.chain.service.transaction

import io.openfuture.chain.domain.transaction.BaseTransactionDto
import io.openfuture.chain.domain.transaction.data.BaseTransactionData
import io.openfuture.chain.entity.transaction.BaseTransaction
import io.openfuture.chain.repository.BaseTransactionRepository
import io.openfuture.chain.service.BaseTransactionService
import io.openfuture.chain.service.TransactionService
import org.springframework.stereotype.Service

@Service
class DefaultTransactionService(
    repository: BaseTransactionRepository<BaseTransaction>
) : DefaultBaseTransactionService<BaseTransaction>(repository), BaseTransactionService<BaseTransaction>