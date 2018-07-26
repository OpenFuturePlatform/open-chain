package io.openfuture.chain.service.transaction

import io.openfuture.chain.component.converter.transaction.impl.DelegateTransactionEntityConverter
import io.openfuture.chain.domain.transaction.BaseTransactionDto
import io.openfuture.chain.domain.transaction.data.DelegateTransactionData
import io.openfuture.chain.entity.Delegate
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.entity.transaction.DelegateTransaction
import io.openfuture.chain.repository.DelegateTransactionRepository
import io.openfuture.chain.service.DelegateService
import io.openfuture.chain.service.DelegateTransactionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultDelegateTransactionService(
    repository: DelegateTransactionRepository,
    entityConverter: DelegateTransactionEntityConverter,
    private val delegateService: DelegateService
) : DefaultManualTransactionService<DelegateTransaction, DelegateTransactionData>(repository, entityConverter),
    DelegateTransactionService {


    @Transactional
    override fun toBlock(dto: BaseTransactionDto<DelegateTransactionData>, block: MainBlock) {
        delegateService.save(Delegate(dto.data.delegateKey, dto.data.senderAddress))
        super.toBlock(dto, block)
    }

    @Transactional
    override fun toBlock(tx: DelegateTransaction, block: MainBlock) {
        delegateService.save(Delegate(tx.delegateKey, tx.senderAddress))
        super.toBlock(tx, block)
    }

}