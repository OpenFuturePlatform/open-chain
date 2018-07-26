package io.openfuture.chain.service.transaction.base

import io.openfuture.chain.entity.transaction.unconfirmed.UTransaction
import io.openfuture.chain.property.ConsensusProperties
import io.openfuture.chain.repository.UTransactionRepository
import io.openfuture.chain.service.BaseTransactionService
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class DefaultBaseTransactionService(
    private val repository: UTransactionRepository<UTransaction>,
    private val consensusProperties: ConsensusProperties
) : BaseTransactionService {

    override fun getPending(): MutableSet<UTransaction> =
        repository.findAllByOrderByFeeDesc(PageRequest.of(0, consensusProperties.blockCapacity!! - 1)).toMutableSet()

}