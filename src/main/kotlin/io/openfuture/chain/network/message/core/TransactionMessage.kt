package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.message.base.Message

@NoArgConstructor
abstract class TransactionMessage(
    var timestamp: Long,
    var fee: Long,
    var senderAddress: String,
    var hash: String,
    var signature: String,
    var publicKey: String
) : Message {

    override fun read(buf: ByteBuf) {
        timestamp = buf.readLong()
        fee = buf.readLong()
        senderAddress = buf.readString()
        hash = buf.readString()
        signature = buf.readString()
        publicKey = buf.readString()
    }

    override fun write(buf: ByteBuf) {
        buf.writeLong(timestamp)
        buf.writeLong(fee)
        buf.writeString(senderAddress)
        buf.writeString(hash)
        buf.writeString(signature)
        buf.writeString(publicKey)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TransactionMessage) return false

        if (timestamp != other.timestamp) return false
        if (fee != other.fee) return false
        if (senderAddress != other.senderAddress) return false
        if (hash != other.hash) return false
        if (signature != other.signature) return false
        if (publicKey != other.publicKey) return false

        return true
    }

    override fun hashCode(): Int {
        var result = timestamp.hashCode()
        result = 31 * result + fee.hashCode()
        result = 31 * result + senderAddress.hashCode()
        result = 31 * result + hash.hashCode()
        result = 31 * result + signature.hashCode()
        result = 31 * result + publicKey.hashCode()
        return result
    }

}
