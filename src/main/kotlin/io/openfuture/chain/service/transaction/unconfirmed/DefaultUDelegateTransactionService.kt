package io.openfuture.chain.service.transaction.unconfirmed

import io.openfuture.chain.component.converter.transaction.impl.UDelegateTransactionEntityConverter
import io.openfuture.chain.domain.rpc.transaction.BaseTransactionRequest
import io.openfuture.chain.domain.transaction.BaseTransactionDto
import io.openfuture.chain.domain.transaction.data.DelegateTransactionData
import io.openfuture.chain.entity.Delegate
import io.openfuture.chain.entity.transaction.unconfirmed.UDelegateTransaction
import io.openfuture.chain.repository.UDelegateTransactionRepository
import io.openfuture.chain.service.DelegateService
import io.openfuture.chain.service.UDelegateTransactionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DefaultUDelegateTransactionService(
    repository: UDelegateTransactionRepository,
    entityConverter: UDelegateTransactionEntityConverter,
    private val delegateService: DelegateService
) : DefaultManualUTransactionService<UDelegateTransaction, DelegateTransactionData>(repository, entityConverter),
    UDelegateTransactionService {

    @Transactional
    override fun validate(request: BaseTransactionRequest<DelegateTransactionData>) {
        baseValidate(request)
    }

    @Transactional
    override fun validate(dto: BaseTransactionDto<DelegateTransactionData>) {
        baseValidate(dto)
    }

    @Transactional
    override fun process(tx: UDelegateTransaction) {
        delegateService.save(Delegate(tx.delegateKey, tx.senderAddress))
    }

}