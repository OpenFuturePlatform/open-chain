package io.openfuture.chain.service.transaction.unconfirmed

import io.openfuture.chain.component.converter.transaction.impl.UDelegateTransactionEntityConverter
import io.openfuture.chain.domain.transaction.data.DelegateTransactionData
import io.openfuture.chain.entity.transaction.unconfirmed.UDelegateTransaction
import io.openfuture.chain.repository.UDelegateTransactionRepository
import io.openfuture.chain.service.UDelegateTransactionService
import org.springframework.stereotype.Service

@Service
class DefaultUDelegateTransactionService(
    repository: UDelegateTransactionRepository,
    entityConverter: UDelegateTransactionEntityConverter
) : DefaultUTransactionService<UDelegateTransaction, DelegateTransactionData, UDelegateTransactionEntityConverter>(repository, entityConverter),
    UDelegateTransactionService