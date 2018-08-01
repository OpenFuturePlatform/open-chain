package io.openfuture.chain.service.transaction.unconfirmed

import io.openfuture.chain.network.domain.application.transaction.DelegateTransactionMessage
import io.openfuture.chain.network.domain.application.transaction.data.DelegateTransactionData
import io.openfuture.chain.entity.transaction.unconfirmed.UDelegateTransaction
import io.openfuture.chain.exception.NotFoundException
import io.openfuture.chain.repository.UDelegateTransactionRepository
import io.openfuture.chain.rpc.domain.transaction.DelegateTransactionRequest
import io.openfuture.chain.service.UDelegateTransactionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultUDelegateTransactionService(
    repository: UDelegateTransactionRepository
) : DefaultUTransactionService<UDelegateTransaction, DelegateTransactionData, DelegateTransactionMessage, DelegateTransactionRequest>(repository),
    UDelegateTransactionService {

    @Transactional(readOnly = true)
    override fun get(hash: String): UDelegateTransaction = repository.findOneByHash(hash)
        ?: throw NotFoundException("Unconfirmed transaction with hash: $hash not exist!")

    @Transactional(readOnly = true)
    override fun getAll(): MutableSet<UDelegateTransaction> = repository.findAll().toMutableSet()

    @Transactional
    override fun add(dto: DelegateTransactionMessage): UDelegateTransaction {
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