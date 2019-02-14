package io.openfuture.chain.core.model.entity.transaction.confirmed

import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.payload.RewardTransactionPayload
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.network.message.core.RewardTransactionMessage
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import java.nio.ByteBuffer
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Table
import kotlin.Long.Companion.SIZE_BYTES

@Entity
@Table(name = "reward_transactions")
class RewardTransaction(
    timestamp: Long,
    fee: Long,
    senderAddress: String,
    hash: String,
    signature: String,
    publicKey: String,

    @Embedded
    private val payload: RewardTransactionPayload,

    block: MainBlock? = null
) : Transaction(timestamp, fee, senderAddress, hash, signature, publicKey, block) {

    companion object {
        fun of(message: RewardTransactionMessage, block: MainBlock? = null): RewardTransaction = RewardTransaction(
            message.timestamp, message.fee, message.senderAddress, message.hash, message.signature, message.publicKey,
            RewardTransactionPayload(message.reward, message.recipientAddress), block
        )

        fun generateHash(timestamp: Long, fee: Long, senderAddress: String, reward: Long, recipientAddress: String): String {
            val bytes = ByteBuffer.allocate(SIZE_BYTES + SIZE_BYTES + senderAddress.toByteArray().size +
                SIZE_BYTES + recipientAddress.toByteArray().size)
                .putLong(timestamp)
                .putLong(fee)
                .put(senderAddress.toByteArray())
                .putLong(reward)
                .put(recipientAddress.toByteArray())
                .array()

            return ByteUtils.toHexString(HashUtils.doubleSha256(bytes))
        }
    }


    override fun toMessage(): RewardTransactionMessage = RewardTransactionMessage(
        timestamp, fee, senderAddress, hash, signature, publicKey, payload.reward, payload.recipientAddress
    )

    override fun getPayload(): RewardTransactionPayload = payload

}