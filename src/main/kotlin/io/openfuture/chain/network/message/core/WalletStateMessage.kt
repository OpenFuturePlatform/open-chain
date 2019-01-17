package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor

@NoArgConstructor
class WalletStateMessage(
    address: String,
    var balance: Long
) : StateMessage(address) {

    override fun read(buf: ByteBuf) {
        super.read(buf)
        balance = buf.readLong()
    }

    override fun write(buf: ByteBuf) {
        super.write(buf)
        buf.writeLong(balance)
    }

}