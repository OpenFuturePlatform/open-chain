package io.openfuture.chain.service.transaction

import io.openfuture.chain.domain.transaction.TransferTransactionDto
import io.openfuture.chain.domain.transaction.data.TransferTransactionData
import io.openfuture.chain.entity.transaction.TransferTransaction
import io.openfuture.chain.entity.transaction.unconfirmed.UTransferTransaction
import io.openfuture.chain.repository.TransferTransactionRepository
import io.openfuture.chain.repository.UTransferTransactionRepository
import io.openfuture.chain.service.TransferTransactionService
import org.springframework.stereotype.Service

@Service
class DefaultTransferTransactionService(
    repository: TransferTransactionRepository,
    uRepository: UTransferTransactionRepository
) : DefaultTransactionService<TransferTransaction, UTransferTransaction, TransferTransactionData, TransferTransactionDto>(repository, uRepository),
    TransferTransactionService