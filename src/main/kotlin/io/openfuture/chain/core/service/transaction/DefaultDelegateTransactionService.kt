package io.openfuture.chain.core.service.transaction

import io.openfuture.chain.core.exception.NotFoundException
import io.openfuture.chain.core.model.dto.transaction.DelegateTransactionDto
import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UDelegateTransaction
import io.openfuture.chain.core.repository.TransactionRepository
import io.openfuture.chain.core.repository.UTransactionRepository
import io.openfuture.chain.core.service.DelegateService
import io.openfuture.chain.core.service.DelegateTransactionService
import io.openfuture.chain.rpc.domain.transaction.DelegateTransactionRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultDelegateTransactionService(
    private val repository: TransactionRepository<DelegateTransaction>,
    private val uRepository: UTransactionRepository<UDelegateTransaction>,
    private val delegateService: DelegateService
) : BaseTransactionService(), DelegateTransactionService {

    @Transactional
    override fun add(dto: DelegateTransactionDto) {
        val transaction = repository.findOneByHash(dto.hash)
        if (null != transaction) {
            return
        }

        val tx = UDelegateTransaction.of(dto)
        validate(tx)
        updateUnconfirmedBalanceByFee(tx)
        uRepository.save(tx)
        // todo broadcast
    }

    @Transactional
    override fun add(request: DelegateTransactionRequest) {
        val tx = request.toUEntity(clock.networkTime())
        validate(tx)
        updateUnconfirmedBalanceByFee(tx)
        uRepository.save(tx)
        // todo broadcast
    }

    @Transactional
    override fun toBlock(hash: String, block: MainBlock) {
        val unconfirmedTx = getUnconfirmedByHash(hash)
        delegateService.save(Delegate(unconfirmedTx.getPayload().delegateKey, unconfirmedTx.senderAddress))

        val tx = unconfirmedTx.toConfirmed()
        tx.block = block
        updateBalanceByFee(tx)
        uRepository.delete(unconfirmedTx)
        repository.save(tx)
    }

    private fun getUnconfirmedByHash(hash: String): UDelegateTransaction = uRepository.findOneByHash(hash)
        ?: throw NotFoundException("Unconfirmed delegate transaction with hash: $hash not found")

}