package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.core.model.entity.transaction.confirmed.RewardTransaction
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString

@NoArgConstructor
class RewardTransactionMessage(
    timestamp: Long,
    fee: Long,
    senderAddress: String,
    hash: String,
    senderSignature: String,
    senderPublicKey: String,
    var reward: Long,
    var recipientAddress: String
) : BaseTransactionMessage(timestamp, fee, senderAddress, hash, senderSignature, senderPublicKey) {

    constructor(tx: RewardTransaction) : this(
        tx.timestamp,
        tx.fee,
        tx.senderAddress,
        tx.hash,
        tx.senderSignature,
        tx.senderPublicKey,
        tx.payload.reward,
        tx.payload.recipientAddress
    )

    override fun read(buffer: ByteBuf) {
        super.read(buffer)
        reward = buffer.readLong()
        recipientAddress = buffer.readString()
    }

    override fun write(buffer: ByteBuf) {
        super.write(buffer)
        buffer.writeLong(reward)
        buffer.writeString(recipientAddress)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RewardTransactionMessage

        if (timestamp != other.timestamp) return false
        if (fee != other.fee) return false
        if (senderAddress != other.senderAddress) return false
        if (hash != other.hash) return false
        if (senderSignature != other.senderSignature) return false
        if (senderPublicKey != other.senderPublicKey) return false
        if (reward != other.reward) return false
        if (recipientAddress != other.recipientAddress) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timestamp.hashCode()
        result = 31 * result + fee.hashCode()
        result = 31 * result + senderAddress.hashCode()
        result = 31 * result + hash.hashCode()
        result = 31 * result + senderSignature.hashCode()
        result = 31 * result + senderPublicKey.hashCode()
        result = 31 * result + reward.hashCode()
        result = 31 * result + recipientAddress.hashCode()
        return result
    }

}