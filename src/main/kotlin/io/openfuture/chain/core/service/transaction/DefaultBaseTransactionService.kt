package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.core.component.TransactionThroughput
import io.openfuture.chain.core.model.entity.transaction.confirmed.Transaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedDelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransferTransaction
import io.openfuture.chain.core.repository.TransactionRepository
import io.openfuture.chain.core.repository.UTransactionRepository
import io.openfuture.chain.core.service.BaseTransactionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class DefaultBaseTransactionService(
    private val repository: TransactionRepository<Transaction>,
    private val uRepository: UTransactionRepository<UnconfirmedTransaction>,
    private val throughput: TransactionThroughput
) : BaseTransactionService {

    override fun getCount(): Long = repository.count()

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
        uRepository.deleteAllByBlockHeightIn(blockHeights)
        uRepository.flush()
    }

}