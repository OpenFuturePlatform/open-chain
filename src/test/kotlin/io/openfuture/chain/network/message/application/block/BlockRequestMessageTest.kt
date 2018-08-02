package io.openfuture.chain.network.message.application.block

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class BlockRequestMessageTest {

    private lateinit var message: BlockRequestMessage
    private lateinit var buffer: ByteBuf

    @Before
    fun setup(){
        buffer = createBuffer("0000000468617368")
        message = BlockRequestMessage("hash")
    }

    @Test
    fun writeShouldWriteExactValuesInBuffer() {
        val actualBuffer = Unpooled.buffer()

        message.write(actualBuffer)

        assertThat(actualBuffer).isEqualTo(buffer)
    }

    @Test
    fun readShouldFillEntityWithExactValuesFromBuffer() {
        val actualBlock = BlockRequestMessage::class.java.newInstance()

        actualBlock.read(buffer)

        assertThat(actualBlock).isEqualToComparingFieldByFieldRecursively(message)
    }

    private fun createBuffer(value: String) : ByteBuf = Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump((value)))

}