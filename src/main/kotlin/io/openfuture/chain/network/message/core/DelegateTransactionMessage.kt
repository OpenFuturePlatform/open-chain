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
    var delegateHost: String,
    var delegatePort: Int,
    var amount: Long
) : TransactionMessage(timestamp, fee, senderAddress, hash, senderSignature, senderPublicKey) {

    override fun read(buf: ByteBuf) {
        super.read(buf)
        nodeId = buf.readString()
        delegateHost = buf.readString()
        delegatePort = buf.readInt()
        amount = buf.readLong()
    }

    override fun write(buf: ByteBuf) {
        super.write(buf)
        buf.writeString(nodeId)
        buf.writeString(delegateHost)
        buf.writeInt(delegatePort)
        buf.writeLong(amount)
    }

}