package io.openfuture.chain.network.message.network.time

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test

class AskTimeMessageTest {

    private lateinit var message: AskTimeMessage
    private lateinit var buffer: ByteBuf


    @Before
    fun setup(){
        buffer = createBuffer("0000001caab5c3b3")
        message = AskTimeMessage(123123123123)
    }

    @Test
    fun writeShouldWriteExactValuesInBuffer() {
        val actualBuffer = Unpooled.buffer()

        message.write(actualBuffer)

        Assertions.assertThat(actualBuffer).isEqualTo(buffer)
    }

    @Test
    fun readShouldFillEntityWithExactValuesFromBuffer() {
        val actualMessage = AskTimeMessage::class.java.newInstance()

        actualMessage.read(buffer)

        Assertions.assertThat(actualMessage).isEqualToComparingFieldByFieldRecursively(message)
    }

    private fun createBuffer(value: String) : ByteBuf = Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump((value)))

}