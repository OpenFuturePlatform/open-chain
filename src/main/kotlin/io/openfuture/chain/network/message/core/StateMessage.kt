package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString
import io.openfuture.chain.network.serialization.Serializable
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils
import java.nio.ByteBuffer

@NoArgConstructor
abstract class StateMessage(
    var address: String
) : Serializable {

    protected abstract fun getBytes(): ByteArray

    fun getHash(): String {
        val bytes = ByteBuffer.allocate(address.toByteArray().size + getBytes().size)
            .put(address.toByteArray())
            .put(getBytes())
            .array()

        return ByteUtils.toHexString(HashUtils.sha256(bytes))
    }

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