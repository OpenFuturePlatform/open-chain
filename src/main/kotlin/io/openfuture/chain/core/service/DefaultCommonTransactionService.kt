package io.openfuture.chain.core.service

import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.transaction.Transaction
import io.openfuture.chain.core.repository.TransactionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultCommonTransactionService(
    private val repository: TransactionRepository<Transaction>
) : CommonTransactionService {

    @Transactional(readOnly = true)
    override fun get(hash: String): Transaction = repository.findOneByHash(hash)
        ?: throw NotFoundException("Transaction with hash: $hash not found")

    @Transactional(readOnly = true)
    override fun isExists(hash: String) : Boolean = null != repository.findOneByHash(hash)

}