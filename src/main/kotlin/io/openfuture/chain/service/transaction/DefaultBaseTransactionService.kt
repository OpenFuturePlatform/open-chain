package io.openfuture.chain.service.transaction

import io.openfuture.chain.entity.transaction.Transaction
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.repository.TransactionRepository
import io.openfuture.chain.service.BaseTransactionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultBaseTransactionService(
    private val repository: TransactionRepository<Transaction>
) : BaseTransactionService {

    @Transactional(readOnly = true)
    override fun get(hash: String): Transaction = repository.findOneByHash(hash)
        ?: throw NotFoundException("Transaction with hash: $hash not exist!")

    @Transactional(readOnly = true)
    override fun isExists(hash: String) : Boolean = null != repository.findOneByHash(hash)

}