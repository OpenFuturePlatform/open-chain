package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString

@NoArgConstructor
class VoteTransactionMessage(
    timestamp: Long,
    fee: Long,
    senderAddress: String,
    hash: String,
    signature: String,
    publicKey: String,
    var voteTypeId: Int,
    var delegateKey: String
) : TransactionMessage(timestamp, fee, senderAddress, hash, signature, publicKey) {

    override fun read(buf: ByteBuf) {
        super.read(buf)
        voteTypeId = buf.readInt()
        delegateKey = buf.readString()
    }

    override fun write(buf: ByteBuf) {
        super.write(buf)
        buf.writeInt(voteTypeId)
        buf.writeString(delegateKey)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VoteTransactionMessage) return false
        if (!super.equals(other)) return false

        if (voteTypeId != other.voteTypeId) return false
        if (delegateKey != other.delegateKey) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + voteTypeId
        result = 31 * result + delegateKey.hashCode()
        return result
    }

}