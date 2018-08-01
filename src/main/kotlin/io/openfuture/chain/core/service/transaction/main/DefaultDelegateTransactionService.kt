package io.openfuture.chain.core.service.transaction.main

import io.openfuture.chain.core.model.dto.transaction.DelegateTransactionDto
import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UDelegateTransaction
import io.openfuture.chain.core.repository.DelegateTransactionRepository
import io.openfuture.chain.core.repository.UDelegateTransactionRepository
import io.openfuture.chain.core.service.DelegateService
import io.openfuture.chain.core.service.DelegateTransactionService
import io.openfuture.chain.rpc.domain.transaction.DelegateTransactionRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultDelegateTransactionService(
    private val repository: DelegateTransactionRepository,
    private val uRepository: UDelegateTransactionRepository,
    private val delegateService: DelegateService
) : BaseTransactionService(), DelegateTransactionService {

    @Transactional
    override fun add(dto: DelegateTransactionDto) {
        val transaction = repository.findOneByHash(dto.hash)
        if (null != transaction) {
            return
        }

        val tx = dto.toUEntity()
        validate(tx)
        updateBalanceByFee(tx)
        uRepository.save(tx)
        // todo broadcast
    }

    @Transactional
    override fun add(request: DelegateTransactionRequest) {
        val tx = request.toUEntity(clock.networkTime())
        validate(tx)
        uRepository.save(tx)
        // todo broadcast
    }

    @Transactional
    override fun toBlock(dto: DelegateTransactionDto, block: MainBlock) {
        delegateService.save(Delegate(dto.data.delegateKey, dto.data.senderAddress))
        val tx = dto.toEntity()
        tx.block = block
        updateBalanceByFee(tx)
        repository.save(tx)

    }

    @Transactional
    override fun toBlock(hash: String, block: MainBlock) {
        val tx = getUnconfirmed(hash)
        val newTx = tx.toConfirmed()
        delegateService.save(Delegate(tx.delegateKey, tx.senderAddress))
        super.processAndSave(newTx, block)
    }

}