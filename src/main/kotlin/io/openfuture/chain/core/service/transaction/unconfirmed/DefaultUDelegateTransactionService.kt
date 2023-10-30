package io.openfuture.chain.core.service.transaction.unconfirmed

import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDelegateTransaction
import io.openfuture.chain.core.repository.UDelegateTransactionRepository
import io.openfuture.chain.core.repository.jdbc.UTransactionsJdbcRepository
import io.openfuture.chain.core.service.UDelegateTransactionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultUDelegateTransactionService(
    uRepository: UDelegateTransactionRepository,
    jdbcRepository: UTransactionsJdbcRepository
) : DefaultUTransactionService<UnconfirmedDelegateTransaction>(uRepository, jdbcRepository), UDelegateTransactionService