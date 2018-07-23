package io.openfuture.chain.component.validator.transaction.impl

import io.openfuture.chain.component.validator.transaction.BaseTransactionValidator
import io.openfuture.chain.domain.rpc.transaction.BaseTransactionRequest
import io.openfuture.chain.domain.transaction.BaseTransactionDto
import io.openfuture.chain.domain.transaction.data.TransferTransactionData
import io.openfuture.chain.entity.transaction.TransferTransaction
import io.openfuture.chain.service.WalletService
import org.springframework.stereotype.Component


@Component
class TransferTransactionValidator(
    walletService: WalletService
) : BaseTransactionValidator<TransferTransaction, TransferTransactionData>(walletService) {

    override fun validate(dto: BaseTransactionDto<TransferTransactionData>) {
        commonValidation(dto.data, dto.senderSignature, dto.senderPublicKey)
    }

    override fun validate(request: BaseTransactionRequest<TransferTransactionData>) {
        commonValidation(request.data!!, request.senderSignature!!, request.senderPublicKey!!)
    }

}