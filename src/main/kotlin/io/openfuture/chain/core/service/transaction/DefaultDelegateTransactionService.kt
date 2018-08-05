package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UDelegateTransaction
import io.openfuture.chain.core.repository.TransactionRepository
import io.openfuture.chain.core.repository.UTransactionRepository
import io.openfuture.chain.core.service.DelegateService
import io.openfuture.chain.core.service.DelegateTransactionService
import io.openfuture.chain.network.message.core.DelegateTransactionMessage
import io.openfuture.chain.rpc.domain.transaction.DelegateTransactionRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.xml.bind.ValidationException

@Service
class DefaultDelegateTransactionService(
    repository: TransactionRepository<DelegateTransaction>,
    uRepository: UTransactionRepository<UDelegateTransaction>,
    private val delegateService: DelegateService
) : BaseTransactionService<DelegateTransaction, UDelegateTransaction>(repository, uRepository), DelegateTransactionService {

    @Transactional
    override fun add(message: DelegateTransactionMessage): UDelegateTransaction {
        val transaction = repository.findOneByHash(message.hash)
        if (null != transaction) {
            return UDelegateTransaction.of(message)
        }

        val tx = UDelegateTransaction.of(message)
        if (!isValid(tx)) {
            throw ValidationException("Transaction is invalid!")
        }

        updateUnconfirmedBalanceByFee(tx)
        // todo broadcast
        return uRepository.save(tx)
    }

    @Transactional
    override fun add(request: DelegateTransactionRequest): UDelegateTransaction {
        val tx = UDelegateTransaction.of(clock.networkTime(), request)
        if (!isValid(tx)) {
            throw ValidationException("Transaction is invalid!")
        }

        updateUnconfirmedBalanceByFee(tx)
        // todo broadcast
        return uRepository.save(tx)
    }

    @Transactional
    override fun toBlock(utx: UDelegateTransaction, block: MainBlock): DelegateTransaction {
        delegateService.save(Delegate(utx.payload.delegateKey, utx.senderAddress))
        return super.toBlock(utx, DelegateTransaction.of(utx), block)
    }

    private fun isValid(tx: UDelegateTransaction): Boolean {
        return isNotExistDelegate(tx.senderAddress) && super.isValid(tx)
    }

    private fun isNotExistDelegate(key: String): Boolean {
        return !delegateService.isExists(key)
    }

}