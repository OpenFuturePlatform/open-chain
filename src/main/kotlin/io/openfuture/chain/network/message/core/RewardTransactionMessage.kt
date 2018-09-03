package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
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
) : TransactionMessage(timestamp, fee, senderAddress, hash, senderSignature, senderPublicKey) {

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
        if (other !is RewardTransactionMessage) return false
        if (!super.equals(other)) return false

        if (reward != other.reward) return false
        if (recipientAddress != other.recipientAddress) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + reward.hashCode()
        result = 31 * result + recipientAddress.hashCode()
        return result
    }


}