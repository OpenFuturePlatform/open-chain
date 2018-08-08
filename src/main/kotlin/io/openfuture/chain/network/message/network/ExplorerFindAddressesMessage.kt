package io.openfuture.chain.network.message.network

import io.netty.buffer.ByteBuf
import io.openfuture.chain.network.annotation.NoArgConstructor
import io.openfuture.chain.network.message.base.BaseMessage

@NoArgConstructor
class ExplorerFindAddressesMessage : BaseMessage {

    override fun read(buffer: ByteBuf) {}

    override fun write(buffer: ByteBuf) {}

    override fun toString() = "ExplorerFindAddressesMessage()"

}