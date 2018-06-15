package io.openfuture.chain.service.impl

import io.openfuture.chain.domain.transaction.TransactionRequest
import io.openfuture.chain.entity.Transaction
import io.openfuture.chain.repository.TransactionRepository
import io.openfuture.chain.service.TransactionService
import org.springframework.stereotype.Service

/**
 * @author Homza Pavel
 */
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