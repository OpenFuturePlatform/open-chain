package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.core.util.ByteConstants.LONG_BYTES
import io.openfuture.chain.network.extension.readString
import io.openfuture.chain.network.extension.writeString
import java.nio.ByteBuffer
import kotlin.text.Charsets.UTF_8

@NoArgConstructor
class DelegateStateMessage(
    address: String,
    var rating: Long,
    var walletAddress: String,
    var createDate: Long
) : StateMessage(address) {

    override fun getBytes(): ByteArray =
        ByteBuffer.allocate(LONG_BYTES + walletAddress.toByteArray(UTF_8).size + LONG_BYTES)
            .putLong(rating)
            .put(walletAddress.toByteArray(UTF_8))
            .putLong(createDate)
            .array()

    override fun read(buf: ByteBuf) {
        super.read(buf)
        rating = buf.readLong()
        walletAddress = buf.readString()
        createDate = buf.readLong()
    }

    override fun write(buf: ByteBuf) {
        super.write(buf)
        buf.writeLong(rating)
        buf.writeString(walletAddress)
        buf.writeLong(createDate)
    }

}