package io.openfuture.chain.component.converter.transaction.impl

import io.openfuture.chain.component.converter.transaction.TransactionEntityConverter
import io.openfuture.chain.domain.rpc.transaction.TransferTransactionRequest
import io.openfuture.chain.domain.transaction.TransferTransactionDto
import io.openfuture.chain.entity.transaction.TransferTransaction
import org.springframework.stereotype.Component

@Component
class TransferTransactionEntityConverter : TransactionEntityConverter<TransferTransaction, TransferTransactionDto,
    TransferTransactionRequest> {

    override fun toEntity(dto: TransferTransactionDto): TransferTransaction = TransferTransaction(
        dto.timestamp,
        dto.amount,
        dto.recipientAddress,
        dto.senderKey,
        dto.senderAddress,
        dto.senderSignature,
        dto.hash
    )

    override fun toEntity(timestamp: Long, request: TransferTransactionRequest): TransferTransaction = TransferTransaction(
        timestamp,
        request.amount!!,
        request.recipientAddress!!,
        request.senderKey!!,
        request.senderAddress!!,
        request.senderSignature!!,
        request.getHash()
    )

}