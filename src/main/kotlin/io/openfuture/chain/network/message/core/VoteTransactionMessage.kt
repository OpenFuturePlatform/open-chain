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

    override fun read(buf: ByteBuf) {
        super.read(buf)
        voteTypeId = buf.readInt()
        nodeId = buf.readString()
    }

    override fun write(buf: ByteBuf) {
        super.write(buf)
        buf.writeInt(voteTypeId)
        buf.writeString(nodeId)
    }

}