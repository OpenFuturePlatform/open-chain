package io.openfuture.chain.component.converter.transaction.impl

import io.openfuture.chain.component.converter.transaction.ManualTransactionEntityConverter
import io.openfuture.chain.domain.rpc.transaction.BaseTransactionRequest
import io.openfuture.chain.domain.transaction.BaseTransactionDto
import io.openfuture.chain.domain.transaction.data.DelegateTransactionData
import io.openfuture.chain.entity.transaction.unconfirmed.UDelegateTransaction
import io.openfuture.chain.util.TransactionUtils
import org.springframework.stereotype.Component

@Component
class UDelegateTransactionEntityConverter : ManualTransactionEntityConverter<UDelegateTransaction, DelegateTransactionData>() {

    override fun toEntity(dto: BaseTransactionDto<DelegateTransactionData>): UDelegateTransaction =
        UDelegateTransaction(
            dto.timestamp,
            dto.data.amount,
            dto.data.fee,
            dto.data.recipientAddress,
            dto.data.senderAddress,
            dto.senderPublicKey,
            dto.senderSignature,
            dto.hash,
            dto.data.delegateKey
        )

    override fun toEntity(timestamp: Long, request: BaseTransactionRequest<DelegateTransactionData>): UDelegateTransaction =
        UDelegateTransaction(
            timestamp,
            request.data!!.amount,
            request.data!!.fee,
            request.data!!.recipientAddress,
            request.data!!.senderAddress,
            request.senderPublicKey!!,
            request.senderSignature!!,
            TransactionUtils.createHash(request.data!!, request.senderPublicKey!!, request.senderSignature!!),
            request.data!!.delegateKey
        )

}