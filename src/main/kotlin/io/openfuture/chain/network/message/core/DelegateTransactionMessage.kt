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
    senderPublicKey: String,
    senderSignature: String,
    hash: String,
    var delegateKey: String
) : BaseTransactionMessage(timestamp, fee, senderAddress, senderPublicKey, senderSignature, hash) {

    override fun read(buffer: ByteBuf) {
        super.read(buffer)
        delegateKey = buffer.readString()
    }

    override fun write(buffer: ByteBuf) {
        super.write(buffer)
        buffer.writeString(delegateKey)
    }

}