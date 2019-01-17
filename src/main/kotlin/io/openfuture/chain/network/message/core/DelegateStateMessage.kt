package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import java.nio.ByteBuffer
import kotlin.Long.Companion.SIZE_BYTES

@NoArgConstructor
class DelegateStateMessage(
    address: String,
    var rating: Long
) : StateMessage(address) {

    override fun getBytes(): ByteArray = ByteBuffer.allocate(SIZE_BYTES).putLong(rating).array()

    override fun read(buf: ByteBuf) {
        super.read(buf)
        rating = buf.readLong()
    }

    override fun write(buf: ByteBuf) {
        super.write(buf)
        buf.writeLong(rating)
    }

}