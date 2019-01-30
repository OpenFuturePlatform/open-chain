package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString
import java.nio.ByteBuffer
import kotlin.text.Charsets.UTF_8

@NoArgConstructor
class ContractStateMessage(
    address: String,
    var storage: String
) : StateMessage(address) {

    override fun getBytes(): ByteArray = ByteBuffer
        .allocate(storage.toByteArray(UTF_8).size)
        .put(storage.toByteArray(UTF_8)).array()

    override fun read(buf: ByteBuf) {
        super.read(buf)
        storage = buf.readString()
    }

    override fun write(buf: ByteBuf) {
        super.write(buf)
        buf.writeString(storage)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ContractStateMessage) return false
        if (!super.equals(other)) return false

        if (storage != other.storage) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + storage.hashCode()
        return result
    }

}