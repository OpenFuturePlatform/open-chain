package io.openfuture.chain.service

import io.openfuture.chain.domain.transaction.TransactionRequest
import io.openfuture.chain.entity.Transaction
import io.openfuture.chain.repository.TransactionRepository
import org.springframework.stereotype.Service

@Service
class DefaultTransactionService(
    private val transactionRepository: TransactionRepository,
    private val blockService: DefaultBlockService
): TransactionService {

    override fun save(request: TransactionRequest) {
        val block = blockService.get(request.blockId)
        transactionRepository.save(Transaction.of(block, request))
    }

}