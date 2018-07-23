package io.openfuture.chain.component.validator.transaction.impl

import io.openfuture.chain.component.validator.transaction.BaseTransactionValidator
import io.openfuture.chain.domain.rpc.transaction.BaseTransactionRequest
import io.openfuture.chain.domain.transaction.BaseTransactionDto
import io.openfuture.chain.domain.transaction.data.VoteTransactionData
import io.openfuture.chain.entity.transaction.VoteTransaction
import io.openfuture.chain.service.WalletService
import org.springframework.stereotype.Component


@Component
class VoteTransactionValidator(
    walletService: WalletService
) : BaseTransactionValidator<VoteTransaction, VoteTransactionData>(walletService) {

    override fun validate(dto: BaseTransactionDto<VoteTransactionData>) {
        commonValidation(dto.data, dto.senderSignature, dto.senderPublicKey)
    }

    override fun validate(request: BaseTransactionRequest<VoteTransactionData>) {
        commonValidation(request.data!!, request.senderSignature!!, request.senderPublicKey!!)
    }

}