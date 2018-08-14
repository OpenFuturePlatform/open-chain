package io.openfuture.chain.core.model.entity.transaction.confirmed

import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.payload.RewardTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.payload.TransactionPayload
import io.openfuture.chain.network.message.core.RewardTransactionMessage
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "reward_transactions")
class RewardTransaction(
    timestamp: Long,
    fee: Long,
    senderAddress: String,
    hash: String,
    senderSignature: String,
    senderPublicKey: String,
    block: MainBlock,

    @Embedded
    val payload: RewardTransactionPayload

) : Transaction(timestamp, fee, senderAddress, hash, senderSignature, senderPublicKey, block) {

    companion object {
        fun of(message: RewardTransactionMessage, block: MainBlock): RewardTransaction = RewardTransaction(
            message.timestamp,
            message.fee,
            message.senderAddress,
            message.hash,
            message.senderSignature,
            message.senderPublicKey,
            block,
            RewardTransactionPayload(message.reward, message.recipientAddress)
        )
    }

    override fun getPayload(): TransactionPayload = payload

}