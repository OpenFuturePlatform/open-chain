package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.core.component.TransactionThroughput
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.entity.transaction.confirmed.Transaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransferTransaction
import io.openfuture.chain.core.repository.TransactionRepository
import io.openfuture.chain.core.repository.UTransactionRepository
import io.openfuture.chain.core.service.TransactionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultTransactionService(
    private val repository: TransactionRepository<Transaction>,
    private val uRepository: UTransactionRepository<UnconfirmedTransaction>,
    private val throughput: TransactionThroughput
) : TransactionService {

    override fun getCount(): Long = repository.count()

    override fun getUnconfirmedTransactionByHash(hash: String): UnconfirmedTransaction =
        uRepository.findOneByHash(hash) ?: throw NotFoundException("Unconfirmed transaction with hash $hash not found")

    override fun getUnconfirmedBalanceBySenderAddress(address: String): Long =
        uRepository.findAllBySenderAddress(address).asSequence().map {
            it.fee + when (it) {
                is UnconfirmedTransferTransaction -> it.getPayload().amount
                is UnconfirmedDelegateTransaction -> it.getPayload().amount
                else -> 0
            }
        }.sum()

    override fun getProducingPerSecond(): Long = throughput.getThroughput()

    @Transactional
    override fun deleteBlockTransactions(blockHeights: List<Long>) {
        repository.deleteAllByBlockHeightIn(blockHeights)
        repository.flush()
    }

}