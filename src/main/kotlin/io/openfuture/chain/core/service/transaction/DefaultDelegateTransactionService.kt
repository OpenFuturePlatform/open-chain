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

@Service
internal class DefaultDelegateTransactionService(
    private val repository: TransactionRepository<DelegateTransaction>,
    private val uRepository: UTransactionRepository<UDelegateTransaction>,
    private val delegateService: DelegateService
) : BaseTransactionService(), DelegateTransactionService {

    @Transactional
    override fun add(message: DelegateTransactionMessage): UDelegateTransaction {
        val transaction = repository.findOneByHash(message.hash)
        if (null != transaction) {
            return UDelegateTransaction.of(message)
        }

        val tx = UDelegateTransaction.of(message)
        validate(tx)
        updateUnconfirmedBalanceByFee(tx)
        // todo broadcast
        return uRepository.save(tx)
    }

    @Transactional
    override fun add(request: DelegateTransactionRequest): UDelegateTransaction {
        val tx = request.toUEntity(clock.networkTime())
        validate(tx)
        updateUnconfirmedBalanceByFee(tx)
        // todo broadcast
        return uRepository.save(tx)
    }

    @Transactional
    override fun toBlock(utx: UDelegateTransaction, block: MainBlock) {
        delegateService.save(Delegate(utx.payload.delegateKey, utx.senderAddress))

        val tx = DelegateTransaction.of(utx)
        tx.block = block
        updateBalanceByFee(tx)
        uRepository.delete(utx)
        repository.save(tx)
    }

}