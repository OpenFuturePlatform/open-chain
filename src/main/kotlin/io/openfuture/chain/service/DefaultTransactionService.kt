package io.openfuture.chain.service

import io.openfuture.chain.domain.transaction.TransactionRequest
import io.openfuture.chain.entity.Transaction
import io.openfuture.chain.repository.TransactionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultTransactionService(
    private val transactionRepository: TransactionRepository,
    private val blockService: BlockService
): TransactionService {

    @Transactional
    override fun save(request: TransactionRequest): Transaction {
        val block = blockService.get(request.blockId)

        return transactionRepository.save(Transaction.of(block, request))
    }

}