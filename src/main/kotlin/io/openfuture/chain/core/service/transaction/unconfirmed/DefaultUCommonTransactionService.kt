package io.openfuture.chain.core.service.transaction.unconfirmed

import io.openfuture.chain.consensus.property.ConsensusProperties
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UTransaction
import io.openfuture.chain.core.repository.UTransactionRepository
import io.openfuture.chain.core.service.UCommonTransactionService
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class DefaultUCommonTransactionService(
    private val repository: UTransactionRepository<UTransaction>,
    private val consensusProperties: ConsensusProperties
) : UCommonTransactionService {

    override fun getAll(): MutableSet<UTransaction> =
        repository.findAllByOrderByFeeDesc(PageRequest.of(0, consensusProperties.blockCapacity!! - 1)).toMutableSet()

}