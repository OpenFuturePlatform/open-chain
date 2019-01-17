package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import java.nio.ByteBuffer

@NoArgConstructor
class WalletStateMessage(
    address: String,
    var balance: Long
) : StateMessage(address) {

    override fun getBytes(): ByteArray = ByteBuffer.allocate(Long.SIZE_BYTES).putLong(balance).array()

    override fun read(buf: ByteBuf) {
        super.read(buf)
        balance = buf.readLong()
    }

    override fun write(buf: ByteBuf) {
        super.write(buf)
        buf.writeLong(balance)
    }

}