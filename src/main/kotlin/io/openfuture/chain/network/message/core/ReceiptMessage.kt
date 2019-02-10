package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.message.base.Message

@NoArgConstructor
class ReceiptMessage(
    var transactionHash: String,
    var result: String,
    var hash: String
) : Message {

    override fun read(buf: ByteBuf) {
        transactionHash = buf.readString()
        result = buf.readString()
        hash = buf.readString()
    }

    override fun write(buf: ByteBuf) {
        buf.writeString(transactionHash)
        buf.writeString(result)
        buf.writeString(hash)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ReceiptMessage

        if (transactionHash != other.transactionHash) return false
        if (result != other.result) return false
        if (hash != other.hash) return false

        return true
    }

    override fun hashCode(): Int {
        var result1 = transactionHash.hashCode()
        result1 = 31 * result1 + result.hashCode()
        result1 = 31 * result1 + hash.hashCode()
        return result1
    }

}