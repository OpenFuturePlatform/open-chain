package io.openfuture.chain.component.converter.transaction.impl

import io.openfuture.chain.component.converter.transaction.EmbeddedTransactionEntityConverter
import io.openfuture.chain.crypto.key.NodeKeyHolder
import io.openfuture.chain.crypto.signature.SignatureManager
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.domain.transaction.BaseTransactionDto
import io.openfuture.chain.domain.transaction.data.RewardTransactionData
import io.openfuture.chain.entity.transaction.RewardTransaction
import org.springframework.stereotype.Component

@Component
class RewardTransactionEntityConverter(
    private val keyHolder: NodeKeyHolder
) : EmbeddedTransactionEntityConverter<RewardTransaction, RewardTransactionData>() {

    override fun toEntity(dto: BaseTransactionDto<RewardTransactionData>): RewardTransaction = RewardTransaction(
        dto.timestamp,
        dto.data.amount,
        dto.data.fee,
        dto.data.recipientAddress,
        dto.data.senderAddress,
        dto.senderPublicKey,
        dto.senderSignature,
        dto.hash
    )

    override fun toEntity(timestamp: Long, data: RewardTransactionData): RewardTransaction =
        RewardTransaction(
            timestamp,
            data.amount,
            data.fee,
            data.recipientAddress,
            data.senderAddress,
            HashUtils.toHexString(keyHolder.getPublicKey()),
            SignatureManager.sign(data.getBytes(), keyHolder.getPrivateKey()),
            getHash(data, keyHolder.getPublicKey(), keyHolder.getPrivateKey())
        )

}