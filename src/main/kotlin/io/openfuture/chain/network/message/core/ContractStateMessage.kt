package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import java.nio.ByteBuffer

@NoArgConstructor
class ContractStateMessage(
    address: String,
    var storage: ByteArray
) : StateMessage(address) {

    override fun getBytes(): ByteArray = ByteBuffer.allocate(storage.size).put(storage).array()

    override fun read(buf: ByteBuf) {
        super.read(buf)
        buf.readBytes(storage)
    }

    override fun write(buf: ByteBuf) {
        super.write(buf)
        buf.writeBytes(storage)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ContractStateMessage) return false
        if (!super.equals(other)) return false

        if (!storage.contentEquals(other.storage)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + storage.contentHashCode()
        return result
    }

}