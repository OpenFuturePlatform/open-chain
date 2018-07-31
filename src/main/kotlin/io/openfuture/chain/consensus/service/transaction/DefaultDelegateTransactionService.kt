package io.openfuture.chain.consensus.service.transaction

import io.openfuture.chain.consensus.model.dto.transaction.DelegateTransactionDto
import io.openfuture.chain.consensus.model.entity.Delegate
import io.openfuture.chain.consensus.model.entity.block.MainBlock
import io.openfuture.chain.consensus.model.entity.transaction.DelegateTransaction
import io.openfuture.chain.consensus.model.entity.transaction.unconfirmed.UDelegateTransaction
import io.openfuture.chain.consensus.repository.DelegateTransactionRepository
import io.openfuture.chain.consensus.repository.UDelegateTransactionRepository
import io.openfuture.chain.consensus.service.DelegateService
import io.openfuture.chain.consensus.service.DelegateTransactionService
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
    override fun toBlock(dto: DelegateTransactionDto, block: MainBlock) {
        delegateService.save(Delegate(dto.data.delegateKey, dto.data.senderAddress))
        super.processAndSave(dto.toEntity(), block)
    }

    @Transactional
    override fun toBlock(hash: String, block: MainBlock) {
        val tx = getUnconfirmed(hash)
        val newTx = tx.toConfirmed()
        delegateService.save(Delegate(tx.delegateKey, tx.senderAddress))
        super.processAndSave(newTx, block)
    }

}