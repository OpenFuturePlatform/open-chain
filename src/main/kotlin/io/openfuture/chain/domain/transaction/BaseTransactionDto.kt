package io.openfuture.chain.domain.transaction

import io.netty.buffer.ByteBuf
import io.openfuture.chain.domain.transaction.data.BaseTransactionData
import io.openfuture.chain.network.domain.NetworkEntity
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString

abstract class BaseTransactionDto<Data : BaseTransactionData>(
    val data: Data,
    var timestamp: Long,
    var senderPublicKey: String,
    var senderSignature: String,
    var hash: String
) : NetworkEntity() {

    override fun read(buffer: ByteBuf) {
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

}
