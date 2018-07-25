package io.openfuture.chain.component.converter.transaction.impl

import io.openfuture.chain.component.converter.transaction.ManualTransactionEntityConverter
import io.openfuture.chain.crypto.key.NodeKeyHolder
import io.openfuture.chain.crypto.signature.SignatureManager
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.domain.rpc.transaction.BaseTransactionRequest
import io.openfuture.chain.domain.transaction.BaseTransactionDto
import io.openfuture.chain.domain.transaction.data.DelegateTransactionData
import io.openfuture.chain.domain.transaction.data.VoteTransactionData
import io.openfuture.chain.entity.transaction.unconfirmed.UVoteTransaction
import org.springframework.stereotype.Component

@Component
class UVoteTransactionEntityConverter(
    private val keyHolder: NodeKeyHolder
) : BaseTransactionEntityConverter<DelegateTransactionData>(),
    ManualTransactionEntityConverter<UVoteTransaction, VoteTransactionData> {

    override fun toEntity(dto: BaseTransactionDto<VoteTransactionData>): UVoteTransaction = UVoteTransaction(
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
            request.data!!.getHash(),
            request.data!!.voteType.getId(),
            request.data!!.delegateKey
        )

    override fun toEntity(timestamp: Long, data: VoteTransactionData): UVoteTransaction =
        UVoteTransaction(
            timestamp,
            data.amount,
            data.fee,
            data.recipientAddress,
            data.senderAddress,
            HashUtils.toHexString(keyHolder.getPublicKey()),
            SignatureManager.sign(data.getBytes(), keyHolder.getPrivateKey()),
            data.getHash(),
            data.voteType.getId(),
            data.delegateKey
        )

}