package io.openfuture.chain.core.service.transaction.unconfirmed

import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransferTransaction
import io.openfuture.chain.core.repository.UTransferTransactionRepository
import io.openfuture.chain.core.service.UTransferTransactionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultUTransferTransactionService(
    uRepository: UTransferTransactionRepository
) : DefaultUTransactionService<UnconfirmedTransferTransaction>(uRepository), UTransferTransactionService