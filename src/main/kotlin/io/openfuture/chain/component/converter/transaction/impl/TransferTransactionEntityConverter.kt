package io.openfuture.chain.component.converter.transaction.impl

import io.openfuture.chain.component.converter.transaction.ManualTransactionEntityConverter
import io.openfuture.chain.domain.rpc.transaction.BaseTransactionRequest
import io.openfuture.chain.domain.transaction.BaseTransactionDto
import io.openfuture.chain.domain.transaction.data.TransferTransactionData
import io.openfuture.chain.entity.transaction.TransferTransaction
import io.openfuture.chain.util.TransactionUtils
import org.springframework.stereotype.Component

@Component
class TransferTransactionEntityConverter : ManualTransactionEntityConverter<TransferTransaction, TransferTransactionData>() {

    override fun toEntity(dto: BaseTransactionDto<TransferTransactionData>): TransferTransaction = TransferTransaction(
        dto.timestamp,
        dto.data.amount,
        dto.data.fee,
        dto.data.recipientAddress,
        dto.data.senderAddress,
        dto.senderPublicKey,
        dto.senderSignature,
        dto.hash
    )

    override fun toEntity(timestamp: Long, request: BaseTransactionRequest<TransferTransactionData>): TransferTransaction =
        TransferTransaction(
            timestamp,
            request.data!!.amount,
            request.data!!.fee,
            request.data!!.recipientAddress,
            request.data!!.senderAddress,
            request.senderPublicKey!!,
            request.senderSignature!!,
            TransactionUtils.createHash(request.data!!, request.senderPublicKey!!, request.senderSignature!!)
        )

}