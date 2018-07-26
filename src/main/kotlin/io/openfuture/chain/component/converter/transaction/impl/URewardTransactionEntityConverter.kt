package io.openfuture.chain.component.converter.transaction.impl

import io.openfuture.chain.component.converter.transaction.EmbeddedTransactionEntityConverter
import io.openfuture.chain.crypto.key.NodeKeyHolder
import io.openfuture.chain.crypto.signature.SignatureManager
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.domain.transaction.BaseTransactionDto
import io.openfuture.chain.domain.transaction.data.RewardTransactionData
import io.openfuture.chain.entity.transaction.unconfirmed.URewardTransaction
import org.springframework.stereotype.Component

@Component
class URewardTransactionEntityConverter(
    private val keyHolder: NodeKeyHolder
) : EmbeddedTransactionEntityConverter<URewardTransaction, RewardTransactionData>() {

    override fun toEntity(dto: BaseTransactionDto<RewardTransactionData>): URewardTransaction =
        URewardTransaction(
            dto.timestamp,
            dto.data.amount,
            dto.data.fee,
            dto.data.recipientAddress,
            dto.data.senderAddress,
            dto.senderPublicKey,
            dto.senderSignature,
            dto.hash
        )

    override fun toEntity(timestamp: Long, data: RewardTransactionData): URewardTransaction =
        URewardTransaction(
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