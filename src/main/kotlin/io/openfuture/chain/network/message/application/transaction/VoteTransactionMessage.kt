package io.openfuture.chain.network.message.application.transaction

import io.netty.buffer.ByteBuf
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString

class VoteTransactionMessage(
    timestamp: Long,
    fee: Long,
    senderAddress: String,
    senderPublicKey: String,
    senderSignature: String,
    hash: String,
    var voteTypeId: Int,
    var delegateKey: String
) : BaseTransactionMessage(timestamp, fee, senderAddress, senderPublicKey, senderSignature, hash) {

    override fun read(buffer: ByteBuf) {
        super.read(buffer)
        voteTypeId = buffer.readInt()
        delegateKey = buffer.readString()
    }

    override fun write(buffer: ByteBuf) {
        super.write(buffer)
        buffer.writeInt(voteTypeId)
        buffer.writeString(delegateKey)
    }

}