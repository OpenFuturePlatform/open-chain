package io.openfuture.chain.component.converter.transaction.impl

import io.openfuture.chain.component.converter.transaction.BaseTransactionEntityConverter
import io.openfuture.chain.domain.rpc.transaction.BaseTransactionRequest
import io.openfuture.chain.domain.transaction.BaseTransactionDto
import io.openfuture.chain.domain.transaction.data.DelegateTransactionData
import io.openfuture.chain.entity.transaction.DelegateTransaction
import org.springframework.stereotype.Component

@Component
class DelegateTransactionEntityConverter : BaseTransactionEntityConverter<DelegateTransaction, DelegateTransactionData>() {

    override fun toEntity(dto: BaseTransactionDto<DelegateTransactionData>): DelegateTransaction = DelegateTransaction(
        dto.timestamp,
        dto.data.amount,
        dto.data.recipientAddress,
        dto.data.senderAddress,
        dto.senderPublicKey,
        dto.senderSignature,
        dto.hash,
        dto.data.delegateKey
    )

    override fun toEntity(timestamp: Long, request: BaseTransactionRequest<DelegateTransactionData>): DelegateTransaction =
        DelegateTransaction(
            timestamp,
            request.data!!.amount,
            request.data!!.recipientAddress,
            request.data!!.senderAddress,
            request.senderPublicKey!!,
            request.senderSignature!!,
            getHash(request),
            request.data!!.delegateKey
        )

}