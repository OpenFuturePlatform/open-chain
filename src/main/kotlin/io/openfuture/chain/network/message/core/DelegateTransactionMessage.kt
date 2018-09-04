package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString

@NoArgConstructor
class DelegateTransactionMessage(
    timestamp: Long,
    fee: Long,
    senderAddress: String,
    hash: String,
    senderSignature: String,
    senderPublicKey: String,
    var nodeId: String,
    var delegateKey: String,
    var delegateHost: String,
    var delegatePort: Int,
    var amount: Long
) : TransactionMessage(timestamp, fee, senderAddress, hash, senderSignature, senderPublicKey) {

    override fun read(buf: ByteBuf) {
        super.read(buf)
        nodeId = buf.readString()
        delegateKey = buf.readString()
        delegateHost = buf.readString()
        delegatePort = buf.readInt()
        amount = buf.readLong()
    }

    override fun write(buf: ByteBuf) {
        super.write(buf)
        buf.writeString(nodeId)
        buf.writeString(delegateKey)
        buf.writeString(delegateHost)
        buf.writeInt(delegatePort)
        buf.writeLong(amount)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DelegateTransactionMessage) return false
        if (!super.equals(other)) return false

        if (nodeId != other.nodeId) return false
        if (delegateKey != other.delegateKey) return false
        if (delegateHost != other.delegateHost) return false
        if (delegatePort != other.delegatePort) return false
        if (amount != other.amount) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + nodeId.hashCode()
        result = 31 * result + delegateKey.hashCode()
        result = 31 * result + delegateHost.hashCode()
        result = 31 * result + delegatePort
        result = 31 * result + amount.hashCode()
        return result
    }

}