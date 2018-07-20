package io.openfuture.chain.component.converter.transaction.impl

import io.openfuture.chain.component.converter.transaction.TransactionEntityConverter
import io.openfuture.chain.domain.rpc.transaction.DelegateTransactionRequest
import io.openfuture.chain.domain.transaction.DelegateTransactionDto
import io.openfuture.chain.entity.transaction.DelegateTransaction
import org.springframework.stereotype.Component

@Component
class DelegateTransactionEntityConverter : TransactionEntityConverter<DelegateTransaction, DelegateTransactionDto,
    DelegateTransactionRequest> {

    override fun toEntity(dto: DelegateTransactionDto): DelegateTransaction = DelegateTransaction(
        dto.timestamp,
        dto.amount,
        dto.recipientAddress,
        dto.senderKey,
        dto.senderAddress,
        dto.senderSignature,
        dto.hash,
        dto.delegateDto.key,
        dto.delegateDto.address,
        dto.delegateDto.networkAddress.host,
        dto.delegateDto.networkAddress.port
    )

    override fun toEntity(timestamp: Long, request: DelegateTransactionRequest): DelegateTransaction = DelegateTransaction(
        timestamp,
        request.amount!!,
        request.recipientAddress!!,
        request.senderKey!!,
        request.senderAddress!!,
        request.senderSignature!!,
        request.getHash(),
        request.delegateDto!!.key,
        request.delegateDto!!.address,
        request.delegateDto!!.networkAddress.host,
        request.delegateDto!!.networkAddress.port
    )

}