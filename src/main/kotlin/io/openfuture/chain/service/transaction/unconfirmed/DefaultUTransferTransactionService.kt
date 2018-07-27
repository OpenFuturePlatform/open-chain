package io.openfuture.chain.service.transaction.unconfirmed

import io.openfuture.chain.domain.rpc.transaction.TransferTransactionRequest
import io.openfuture.chain.domain.transaction.TransferTransactionDto
import io.openfuture.chain.domain.transaction.data.TransferTransactionData
import io.openfuture.chain.entity.transaction.TransferTransaction
import io.openfuture.chain.entity.transaction.unconfirmed.UTransferTransaction
import io.openfuture.chain.repository.UTransferTransactionRepository
import io.openfuture.chain.service.UTransferTransactionService
import org.springframework.stereotype.Service

@Service
class DefaultUTransferTransactionService(
    repository: UTransferTransactionRepository
) : DefaultUTransactionService<TransferTransaction, UTransferTransaction, TransferTransactionData, TransferTransactionDto, TransferTransactionRequest>(repository),
    UTransferTransactionService