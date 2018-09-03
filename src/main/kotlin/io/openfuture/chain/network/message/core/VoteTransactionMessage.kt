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
    senderSignature: String,
    senderPublicKey: String,
    var voteTypeId: Int,
    var nodeId: String
) : TransactionMessage(timestamp, fee, senderAddress, hash, senderSignature, senderPublicKey) {

    override fun read(buffer: ByteBuf) {
        super.read(buffer)
        voteTypeId = buffer.readInt()
        nodeId = buffer.readString()
    }

    override fun write(buffer: ByteBuf) {
        super.write(buffer)
        buffer.writeInt(voteTypeId)
        buffer.writeString(nodeId)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is VoteTransactionMessage) return false
        if (!super.equals(other)) return false

        if (voteTypeId != other.voteTypeId) return false
        if (nodeId != other.nodeId) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + voteTypeId
        result = 31 * result + nodeId.hashCode()
        return result
    }

}