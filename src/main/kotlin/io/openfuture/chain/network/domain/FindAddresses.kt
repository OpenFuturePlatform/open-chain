package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf

class FindAddresses : Packet() {

    override fun get(buffer: ByteBuf) {}

    override fun send(buffer: ByteBuf) {}

    override fun toString() = "FindAddresses()"

}