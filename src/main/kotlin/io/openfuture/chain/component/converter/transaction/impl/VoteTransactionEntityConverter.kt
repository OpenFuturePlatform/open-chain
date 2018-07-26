package io.openfuture.chain.component.converter.transaction.impl

import io.openfuture.chain.component.converter.transaction.ManualTransactionEntityConverter
import io.openfuture.chain.domain.rpc.transaction.BaseTransactionRequest
import io.openfuture.chain.domain.transaction.BaseTransactionDto
import io.openfuture.chain.domain.transaction.data.VoteTransactionData
import io.openfuture.chain.entity.transaction.VoteTransaction
import io.openfuture.chain.util.TransactionUtils
import org.springframework.stereotype.Component

@Component
class VoteTransactionEntityConverter : ManualTransactionEntityConverter<VoteTransaction, VoteTransactionData>() {

    override fun toEntity(dto: BaseTransactionDto<VoteTransactionData>): VoteTransaction = VoteTransaction(
        dto.timestamp,
        dto.data.amount,
        dto.data.fee,
        dto.data.recipientAddress,
        dto.data.senderAddress,
        dto.senderPublicKey,
        dto.senderSignature,
        dto.hash,
        dto.data.voteTypeId,
        dto.data.delegateKey
    )

    override fun toEntity(timestamp: Long, request: BaseTransactionRequest<VoteTransactionData>): VoteTransaction =
        VoteTransaction(
            timestamp,
            request.data!!.amount,
            request.data!!.fee,
            request.data!!.recipientAddress,
            request.data!!.senderAddress,
            request.senderPublicKey!!,
            request.senderSignature!!,
            TransactionUtils.createHash(request.data!!, request.senderPublicKey!!, request.senderSignature!!),
            request.data!!.voteTypeId,
            request.data!!.delegateKey
        )

}