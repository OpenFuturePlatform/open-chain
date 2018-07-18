package io.openfuture.chain.component.converter.transaction.impl

import io.openfuture.chain.component.converter.transaction.TransactionEntityConverter
import io.openfuture.chain.domain.rpc.transaction.VoteTransactionRequest
import io.openfuture.chain.domain.transaction.VoteTransactionDto
import io.openfuture.chain.entity.transaction.VoteTransaction
import org.springframework.stereotype.Component

@Component
class VoteTransactionEntityConverter : TransactionEntityConverter<VoteTransaction, VoteTransactionDto,
    VoteTransactionRequest> {

    override fun toEntity(dto: VoteTransactionDto): VoteTransaction = VoteTransaction(
        dto.timestamp,
        dto.amount,
        dto.recipientAddress,
        dto.senderKey,
        dto.senderAddress,
        dto.senderSignature,
        dto.hash,
        dto.voteType.getId(),
        dto.delegateKey
    )

    override fun toEntity(timestamp: Long, request: VoteTransactionRequest): VoteTransaction = VoteTransaction(
        timestamp,
        request.amount!!,
        request.recipientAddress!!,
        request.senderKey!!,
        request.senderAddress!!,
        request.senderSignature!!,
        request.getHash(),
        request.voteType!!.getId(),
        request.delegateKey!!
    )

}