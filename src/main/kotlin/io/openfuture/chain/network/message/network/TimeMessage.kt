package io.openfuture.chain.network.message.network

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.serialization.Serializable

@NoArgConstructor
data class TimeMessage (
    var isSynchronized: Boolean = false,
    var originalTime: Long = 0,
    var receiveTime: Long = 0,
    var transmitTime: Long = 0,
    var destinationTime: Long = 0
) : Serializable {

    override fun read(buf: ByteBuf) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun write(buf: ByteBuf) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun isValidRequest(): Boolean = 0L != originalTime

    fun isValidResponse(): Boolean = 0L != originalTime && 0L != receiveTime && 0L != transmitTime

}