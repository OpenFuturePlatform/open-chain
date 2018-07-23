package io.openfuture.chain.component.validator.transaction.impl

import io.openfuture.chain.component.validator.transaction.BaseTransactionValidator
import io.openfuture.chain.domain.rpc.transaction.BaseTransactionRequest
import io.openfuture.chain.domain.transaction.BaseTransactionDto
import io.openfuture.chain.domain.transaction.data.DelegateTransactionData
import io.openfuture.chain.entity.transaction.DelegateTransaction
import io.openfuture.chain.service.WalletService
import org.springframework.stereotype.Component


@Component
class DelegateTransactionValidator(
    walletService: WalletService
) : BaseTransactionValidator<DelegateTransaction, DelegateTransactionData>(walletService) {

    override fun validate(dto: BaseTransactionDto<DelegateTransactionData>) {
        commonValidation(dto.data, dto.senderSignature, dto.senderPublicKey)
    }

    override fun validate(request: BaseTransactionRequest<DelegateTransactionData>) {
        commonValidation(request.data!!, request.senderSignature!!, request.senderPublicKey!!)
    }

}