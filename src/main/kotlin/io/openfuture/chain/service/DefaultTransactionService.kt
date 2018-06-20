package io.openfuture.chain.service

import io.openfuture.chain.domain.transaction.TransactionRequest
import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.Transaction
import io.openfuture.chain.repository.TransactionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultTransactionService(
    private val transactionRepository: TransactionRepository
): TransactionService {

    @Transactional
    override fun save(block: Block, request: TransactionRequest): Transaction {
        return transactionRepository.save(Transaction.of(block, request))
    }

}