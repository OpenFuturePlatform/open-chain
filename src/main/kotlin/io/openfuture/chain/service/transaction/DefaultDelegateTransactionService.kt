package io.openfuture.chain.service.transaction

import io.openfuture.chain.network.domain.application.transaction.DelegateTransactionMessage
import io.openfuture.chain.entity.Delegate
import io.openfuture.chain.entity.block.MainBlock
import io.openfuture.chain.entity.transaction.DelegateTransaction
import io.openfuture.chain.entity.transaction.unconfirmed.UDelegateTransaction
import io.openfuture.chain.repository.DelegateTransactionRepository
import io.openfuture.chain.repository.UDelegateTransactionRepository
import io.openfuture.chain.service.DelegateService
import io.openfuture.chain.service.DelegateTransactionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultDelegateTransactionService(
    repository: DelegateTransactionRepository,
    uRepository: UDelegateTransactionRepository,
    private val delegateService: DelegateService
) : DefaultTransactionService<DelegateTransaction, UDelegateTransaction>(repository, uRepository),
    DelegateTransactionService {

    @Transactional
    override fun toBlock(dto: DelegateTransactionMessage, block: MainBlock) {
        delegateService.save(Delegate(dto.data.delegateKey, dto.data.senderAddress))
        super.processAndSave(dto.toEntity(), block)
    }

    @Transactional
    override fun toBlock(hash: String, block: MainBlock) {
        val tx = get(hash)
        val newTx = tx.toConfirmed()
        delegateService.save(Delegate(tx.delegateKey, tx.senderAddress))
        super.processAndSave(newTx, block)
    }

}