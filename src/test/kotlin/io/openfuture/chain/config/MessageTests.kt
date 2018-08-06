package io.openfuture.chain.config

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled

abstract class MessageTests {

    protected fun createBuffer(hexDump: String): ByteBuf =
        Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump(hexDump))

}