package io.openfuture.chain.component.converter.transaction.unconfirmed.impl

import io.openfuture.chain.component.converter.transaction.TransactionEntityConverter
import io.openfuture.chain.crypto.key.NodeKeyHolder
import io.openfuture.chain.crypto.signature.SignatureManager
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.domain.rpc.transaction.BaseTransactionRequest
import io.openfuture.chain.domain.transaction.BaseTransactionDto
import io.openfuture.chain.domain.transaction.data.TransferTransactionData
import io.openfuture.chain.entity.transaction.unconfirmed.UTransferTransaction
import org.springframework.stereotype.Component

@Component
class UTransferTransactionEntityConverter(
    private val keyHolder: NodeKeyHolder
) : TransactionEntityConverter<UTransferTransaction, TransferTransactionData> {

    override fun toEntity(dto: BaseTransactionDto<TransferTransactionData>): UTransferTransaction = UTransferTransaction(
        dto.timestamp,
        dto.data.amount,
        dto.data.recipientAddress,
        dto.data.senderAddress,
        dto.senderPublicKey,
        dto.senderSignature,
        dto.hash
    )

    override fun toEntity(timestamp: Long, request: BaseTransactionRequest<TransferTransactionData>): UTransferTransaction =
        UTransferTransaction(
            timestamp,
            request.data!!.amount,
            request.data!!.recipientAddress,
            request.data!!.senderAddress,
            request.senderPublicKey!!,
            request.senderSignature!!,
            request.data!!.getHash()
        )

    override fun toEntity(timestamp: Long, data: TransferTransactionData): UTransferTransaction =
        UTransferTransaction(
            timestamp,
            data.amount,
            data.recipientAddress,
            data.senderAddress,
            HashUtils.toHexString(keyHolder.getPublicKey()),
            SignatureManager.sign(data.getBytes(), keyHolder.getPrivateKey()),
            data.getHash()
        )

}