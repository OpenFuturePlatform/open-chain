package io.openfuture.chain.service.transaction.unconfirmed

import io.openfuture.chain.component.converter.transaction.impl.UTransferTransactionEntityConverter
import io.openfuture.chain.domain.transaction.data.TransferTransactionData
import io.openfuture.chain.entity.transaction.unconfirmed.UTransferTransaction
import io.openfuture.chain.repository.UTransferTransactionRepository
import io.openfuture.chain.service.UTransferTransactionService
import org.springframework.stereotype.Service

@Service
class DefaultUTransferTransactionService(
    repository: UTransferTransactionRepository,
    entityConverter: UTransferTransactionEntityConverter
) : DefaultManualUTransactionService<UTransferTransaction, TransferTransactionData>(repository, entityConverter),
    UTransferTransactionService