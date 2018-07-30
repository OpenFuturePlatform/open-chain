package io.openfuture.chain.service.transaction.base

import io.openfuture.chain.entity.transaction.Transaction
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.repository.TransactionRepository
import io.openfuture.chain.service.CommonTransactionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultCommonTransactionService(
    private val repository: TransactionRepository<Transaction>
) : CommonTransactionService {

    @Transactional(readOnly = true)
    override fun get(hash: String): Transaction = repository.findOneByHash(hash)
        ?: throw NotFoundException("Transaction with hash: $hash not exist!")

    @Transactional(readOnly = true)
    override fun isExists(hash: String) : Boolean = null != repository.findOneByHash(hash)

}