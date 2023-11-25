package io.openfuture.chain.tendermint.service

import io.openfuture.chain.core.model.entity.tendermint.TendermintTransferTransaction
import io.openfuture.chain.core.repository.TendermintTransferTransactionRepository
import io.openfuture.chain.core.service.TendermintTransferTransactionService
import io.openfuture.chain.tendermint.repository.TendermintTransactionsJdbcRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultTendermintTransferTransactionService(
    uRepository: TendermintTransferTransactionRepository,
    jdbcRepository: TendermintTransactionsJdbcRepository
) : DefaultTendermintTransactionService<TendermintTransferTransaction>(uRepository, jdbcRepository),TendermintTransferTransactionService {
}