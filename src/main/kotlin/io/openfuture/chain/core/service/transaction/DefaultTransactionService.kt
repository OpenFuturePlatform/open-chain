package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.core.component.TransactionCapacityChecker
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.transaction.confirmed.Transaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransaction
import io.openfuture.chain.core.repository.TransactionRepository
import io.openfuture.chain.core.repository.UTransactionRepository
import io.openfuture.chain.core.service.TransactionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultTransactionService(
    private val repository: TransactionRepository<Transaction>,
    private val uRepository: UTransactionRepository<UnconfirmedTransaction>,
    private val capacityChecker: TransactionCapacityChecker
) : TransactionService {

    @Transactional(readOnly = true)
    override fun getAllUnconfirmedByAddress(address: String): List<UnconfirmedTransaction> =
        uRepository.findAllByHeaderSenderAddress(address)

    @Transactional(readOnly = true)
    override fun getCount(): Long = repository.count()

    @Transactional(readOnly = true)
    override fun getUnconfirmedTransactionByHash(hash: String): UnconfirmedTransaction = uRepository.findOneByFooterHash(hash)
        ?: throw NotFoundException("Unconfirmed transaction with hash $hash not found")

    override fun getProducingPerSecond(): Long {
        return capacityChecker.getCountPerSecond()
    }

}