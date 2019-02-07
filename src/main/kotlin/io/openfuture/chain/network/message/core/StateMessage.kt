package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.message.base.Message

@NoArgConstructor
abstract class StateMessage(
    var address: String,
    var hash: String
) : Message {

    override fun read(buf: ByteBuf) {
        address = buf.readString()
        hash = buf.readString()
    }

    override fun write(buf: ByteBuf) {
        buf.writeString(address)
        buf.writeString(hash)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StateMessage

        if (address != other.address) return false
        if (hash != other.hash) return false

        return true
    }

    override fun hashCode(): Int {
        var result = address.hashCode()
        result = 31 * result + hash.hashCode()
        return result
    }

}