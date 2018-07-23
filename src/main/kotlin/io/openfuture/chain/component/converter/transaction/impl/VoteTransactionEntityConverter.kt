package io.openfuture.chain.component.converter.transaction.impl

import io.openfuture.chain.component.converter.transaction.BaseTransactionEntityConverter
import io.openfuture.chain.crypto.key.NodeKeyHolder
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.domain.rpc.transaction.BaseTransactionRequest
import io.openfuture.chain.domain.transaction.BaseTransactionDto
import io.openfuture.chain.domain.transaction.data.VoteTransactionData
import io.openfuture.chain.entity.transaction.VoteTransaction
import org.springframework.stereotype.Component

@Component
class VoteTransactionEntityConverter(
    private val keyHolder: NodeKeyHolder
) : BaseTransactionEntityConverter<VoteTransaction, VoteTransactionData>() {

    override fun toEntity(dto: BaseTransactionDto<VoteTransactionData>): VoteTransaction = VoteTransaction(
        dto.timestamp,
        dto.data.amount ,
        dto.data.recipientAddress,
        dto.data.senderAddress,
        dto.senderPublicKey,
        dto.senderSignature,
        dto.hash,
        dto.data.voteType.getId(),
        dto.data.delegateKey
    )

    override fun toEntity(timestamp: Long, request: BaseTransactionRequest<VoteTransactionData>): VoteTransaction =
        VoteTransaction(
            timestamp,
            request.data!!.amount,
            request.data!!.recipientAddress,
            request.data!!.senderAddress,
            request.senderPublicKey!!,
            request.senderSignature!!,
            getHash(request),
            request.data!!.voteType.getId(),
            request.data!!.delegateKey
        )

    override fun toEntity(timestamp: Long, data: VoteTransactionData): VoteTransaction =
        VoteTransaction(
            timestamp,
            data.amount,
            data.recipientAddress,
            data.senderAddress,
            HashUtils.toHexString(keyHolder.getPublicKey()),
            getSignature(data, keyHolder.getPrivateKey()),
            getHash(data, keyHolder.getPublicKey(), keyHolder.getPrivateKey()),
            data.voteType.getId(),
            data.delegateKey
        )

}