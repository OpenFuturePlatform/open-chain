package io.openfuture.chain.consensus.service.transaction.unconfirmed

import io.openfuture.chain.consensus.model.entity.transaction.unconfirmed.UDelegateTransaction
import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.consensus.repository.UDelegateTransactionRepository
import io.openfuture.chain.consensus.service.UDelegateTransactionService
import io.openfuture.chain.consensus.model.dto.transaction.DelegateTransactionDto
import io.openfuture.chain.consensus.model.dto.transaction.data.DelegateTransactionData
import io.openfuture.chain.rpc.domain.transaction.DelegateTransactionRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultUDelegateTransactionService(
    repository: UDelegateTransactionRepository
) : DefaultUTransactionService<UDelegateTransaction, DelegateTransactionData, DelegateTransactionDto, DelegateTransactionRequest>(repository),
    UDelegateTransactionService {

    @Transactional(readOnly = true)
    override fun get(hash: String): UDelegateTransaction = repository.findOneByHash(hash)
        ?: throw NotFoundException("Unconfirmed transaction with hash: $hash not exist!")

    @Transactional(readOnly = true)
    override fun getAll(): MutableSet<UDelegateTransaction> = repository.findAll().toMutableSet()

    @Transactional
    override fun add(dto: DelegateTransactionDto): UDelegateTransaction {
        val transaction = repository.findOneByHash(dto.hash)
        if (null != transaction) {
            return transaction
        }
        validate(dto)
        return saveAndBroadcast(dto.toUEntity())
    }

    @Transactional
    override fun add(request: DelegateTransactionRequest): UDelegateTransaction {
        validate(request)
        return saveAndBroadcast(request.toEntity(nodeClock.networkTime()))
    }

}