package io.openfuture.chain.service.transaction

import io.openfuture.chain.entity.transaction.unconfirmed.UTransaction
import io.openfuture.chain.property.ConsensusProperties
import io.openfuture.chain.repository.UTransactionRepository
import io.openfuture.chain.service.BaseUTransactionService
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class DefaultBaseUTransactionService(
    private val repository: UTransactionRepository<UTransaction>,
    private val consensusProperties: ConsensusProperties
) : BaseUTransactionService {

    override fun getPending(): MutableSet<UTransaction> =
        repository.findAllByOrderByFeeDesc(PageRequest.of(0, consensusProperties.blockCapacity!! - 1)).toMutableSet()

}