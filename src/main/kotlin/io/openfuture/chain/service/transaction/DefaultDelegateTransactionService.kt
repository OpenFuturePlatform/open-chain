package io.openfuture.chain.service.transaction

import io.openfuture.chain.entity.transaction.DelegateTransaction
import io.openfuture.chain.entity.transaction.unconfirmed.UDelegateTransaction
import io.openfuture.chain.repository.DelegateTransactionRepository
import io.openfuture.chain.repository.UDelegateTransactionRepository
import io.openfuture.chain.service.DelegateTransactionService
import org.springframework.stereotype.Service

@Service
class DefaultDelegateTransactionService(
    repository: DelegateTransactionRepository,
    uRepository: UDelegateTransactionRepository
) : DefaultTransactionService<DelegateTransaction, UDelegateTransaction>(repository, uRepository),
    DelegateTransactionService