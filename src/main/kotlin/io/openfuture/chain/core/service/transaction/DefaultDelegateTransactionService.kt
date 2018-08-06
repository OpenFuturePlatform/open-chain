package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.payload.DelegateTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UDelegateTransaction
import io.openfuture.chain.core.repository.TransactionRepository
import io.openfuture.chain.core.repository.UTransactionRepository
import io.openfuture.chain.core.service.DelegateService
import io.openfuture.chain.core.service.DelegateTransactionService
import io.openfuture.chain.core.util.TransactionUtils
import io.openfuture.chain.network.message.core.DelegateTransactionMessage
import io.openfuture.chain.network.service.NetworkService
import io.openfuture.chain.rpc.domain.transaction.request.delegate.DelegateTransactionHashRequest
import io.openfuture.chain.rpc.domain.transaction.request.delegate.DelegateTransactionRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.xml.bind.ValidationException

@Service
class DefaultDelegateTransactionService(
    repository: TransactionRepository<DelegateTransaction>,
    uRepository: UTransactionRepository<UDelegateTransaction>,
    private val delegateService: DelegateService,
    private val networkService: NetworkService
) : BaseTransactionService<DelegateTransaction, UDelegateTransaction>(repository, uRepository), DelegateTransactionService {

    @Transactional
    override fun add(message: DelegateTransactionMessage): UDelegateTransaction {
        val transaction = repository.findOneByHash(message.hash)
        if (null != transaction) {
            return UDelegateTransaction.of(message)
        }

        val utx = UDelegateTransaction.of(message)
        if (!isValid(utx)) {
            throw ValidationException("Transaction is invalid!")
        }

        val savedUtx = super.add(utx)
        networkService.broadcast(message)
        return savedUtx
    }

    @Transactional
    override fun add(request: DelegateTransactionRequest): UDelegateTransaction {
        val utx = UDelegateTransaction.of(clock.networkTime(), request)
        if (!isValid(utx)) {
            throw ValidationException("Transaction is invalid!")
        }

        val savedUtx = super.add(utx)
        networkService.broadcast(DelegateTransactionMessage(savedUtx))
        return savedUtx
    }

    override fun generateHash(request: DelegateTransactionHashRequest): String {
        return TransactionUtils.generateHash(request.timestamp!!, request.fee!!,
            DelegateTransactionPayload(request.delegateKey!!))
    }

    @Transactional
    override fun toBlock(utx: UDelegateTransaction, block: MainBlock): DelegateTransaction {
        delegateService.save(Delegate(utx.payload.delegateKey, utx.senderAddress))
        return super.toBlock(utx, DelegateTransaction.of(utx), block)
    }

    private fun isValid(utx: UDelegateTransaction): Boolean {
        return isNotExistDelegate(utx.senderAddress) && super.isValid(utx)
    }

    private fun isNotExistDelegate(key: String): Boolean {
        return !delegateService.isExists(key)
    }

}