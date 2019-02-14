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
    signature: String,
    publicKey: String,
    var reward: Long,
    var recipientAddress: String
) : TransactionMessage(timestamp, fee, senderAddress, hash, signature, publicKey) {

    override fun read(buf: ByteBuf) {
        super.read(buf)
        reward = buf.readLong()
        recipientAddress = buf.readString()
    }

    override fun write(buf: ByteBuf) {
        super.write(buf)
        buf.writeLong(reward)
        buf.writeString(recipientAddress)
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