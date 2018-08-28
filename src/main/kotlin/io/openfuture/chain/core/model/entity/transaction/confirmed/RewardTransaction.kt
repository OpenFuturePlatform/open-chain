package io.openfuture.chain.core.model.entity.transaction.confirmed

import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.TransactionFooter
import io.openfuture.chain.core.model.entity.transaction.TransactionHeader
import io.openfuture.chain.core.model.entity.transaction.payload.RewardTransactionPayload
import io.openfuture.chain.network.message.core.RewardTransactionMessage
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "reward_transactions")
class RewardTransaction(
    header: TransactionHeader,
    footer: TransactionFooter,
    block: MainBlock,

    @Embedded
    val payload: RewardTransactionPayload

) : Transaction(header, footer, block) {

    companion object {
        fun of(message: RewardTransactionMessage, block: MainBlock): RewardTransaction = RewardTransaction(
            TransactionHeader(message.timestamp, message.fee, message.senderAddress),
            TransactionFooter(message.hash, message.senderSignature, message.senderPublicKey),
            block,
            RewardTransactionPayload(message.reward, message.recipientAddress)
        )
    }

    override fun toMessage(): RewardTransactionMessage = RewardTransactionMessage(
        header.timestamp,
        header.fee,
        header.senderAddress,
        footer.hash,
        footer.senderSignature,
        footer.senderPublicKey,
        payload.reward,
        payload.recipientAddress
    )

}