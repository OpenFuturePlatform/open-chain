package io.openfuture.chain.component.converter.transaction.impl

import io.openfuture.chain.component.converter.transaction.ManualTransactionEntityConverter
import io.openfuture.chain.domain.rpc.transaction.BaseTransactionRequest
import io.openfuture.chain.domain.transaction.BaseTransactionDto
import io.openfuture.chain.domain.transaction.data.VoteTransactionData
import io.openfuture.chain.entity.transaction.unconfirmed.UVoteTransaction
import org.springframework.stereotype.Component

@Component
class UVoteTransactionEntityConverter : ManualTransactionEntityConverter<UVoteTransaction, VoteTransactionData>() {

    override fun toEntity(dto: BaseTransactionDto<VoteTransactionData>): UVoteTransaction =
        UVoteTransaction(
            dto.timestamp,
            dto.data.amount,
            dto.data.fee,
            dto.data.recipientAddress,
            dto.data.senderAddress,
            dto.senderPublicKey,
            dto.senderSignature,
            dto.hash,
            dto.data.voteType.getId(),
            dto.data.delegateKey
        )

    override fun toEntity(timestamp: Long, request: BaseTransactionRequest<VoteTransactionData>): UVoteTransaction =
        UVoteTransaction(
            timestamp,
            request.data!!.amount,
            request.data!!.fee,
            request.data!!.recipientAddress,
            request.data!!.senderAddress,
            request.senderPublicKey!!,
            request.senderSignature!!,
            getHash(request),
            request.data!!.voteType.getId(),
            request.data!!.delegateKey
        )

}