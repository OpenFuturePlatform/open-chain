package io.openfuture.chain.network.message.network

import io.netty.buffer.ByteBuf
import io.openfuture.chain.core.annotation.NoArgConstructor
import io.openfuture.chain.network.message.base.BaseMessage

@NoArgConstructor
class FindAddressesMessage : BaseMessage {

    override fun read(buffer: ByteBuf) {}

    override fun write(buffer: ByteBuf) {}

}