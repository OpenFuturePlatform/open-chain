package io.openfuture.chain.network.domain.application.transaction

import io.netty.buffer.ByteBuf
import io.openfuture.chain.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.domain.base.BaseMessage
import io.openfuture.chain.network.domain.application.transaction.data.BaseTransactionData

@NoArgConstructor
abstract class BaseTransactionMessage<Data : BaseTransactionData>(
    var data: Data,
    var timestamp: Long,
    var senderPublicKey: String,
    var senderSignature: String,
    var hash: String
) : BaseMessage {

    override fun read(buffer: ByteBuf) {
        data = getDataInstance()
        data.read(buffer)

        timestamp = buffer.readLong()
        senderPublicKey = buffer.readString()
        senderSignature = buffer.readString()
        hash = buffer.readString()
    }

    override fun write(buffer: ByteBuf) {
        data.write(buffer)

        buffer.writeLong(timestamp)
        buffer.writeString(senderPublicKey)
        buffer.writeString(senderSignature)
        buffer.writeString(hash)
    }

    abstract fun getDataInstance() : Data

}
