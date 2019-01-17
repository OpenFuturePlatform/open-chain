package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.serialization.Serializable

@NoArgConstructor
abstract class StateMessage(
    var address: String
) : Serializable {

    override fun read(buf: ByteBuf) {
        address = buf.readString()
    }

    override fun write(buf: ByteBuf) {
        buf.writeString(address)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StateMessage

        if (address != other.address) return false

        return true
    }

    override fun hashCode(): Int {
        return address.hashCode()
    }

}