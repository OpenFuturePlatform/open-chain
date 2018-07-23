package io.openfuture.chain.component.converter.transaction.impl

import io.openfuture.chain.component.converter.transaction.BaseTransactionEntityConverter
import io.openfuture.chain.crypto.key.NodeKeyHolder
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.domain.rpc.transaction.BaseTransactionRequest
import io.openfuture.chain.domain.transaction.BaseTransactionDto
import io.openfuture.chain.domain.transaction.data.TransferTransactionData
import io.openfuture.chain.entity.transaction.TransferTransaction
import org.springframework.stereotype.Component

@Component
class TransferTransactionEntityConverter(
    private val keyHolder: NodeKeyHolder
) : BaseTransactionEntityConverter<TransferTransaction, TransferTransactionData>() {

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
            getHash(request)
        )

    override fun toEntity(timestamp: Long, data: TransferTransactionData): TransferTransaction =
        TransferTransaction(
            timestamp,
            data.amount,
            data.fee,
            data.recipientAddress,
            data.senderAddress,
            HashUtils.toHexString(keyHolder.getPublicKey()),
            getSignature(data, keyHolder.getPrivateKey()),
            getHash(data, keyHolder.getPublicKey(), keyHolder.getPrivateKey())
        )

}