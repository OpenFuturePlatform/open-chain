package io.openfuture.chain.network.message.network

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled
import io.openfuture.chain.network.message.network.HeartBeatMessage.Type.PING
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test

class HeartBeatMessageTest {

    private lateinit var message: HeartBeatMessage
    private lateinit var buffer: ByteBuf


    @Before
    fun setup() {
        buffer = createBuffer("01")
        message = HeartBeatMessage(PING)
    }

    @Test
    fun writeShouldWriteExactValuesInBuffer() {
        val actualBuffer = Unpooled.buffer()

        message.write(actualBuffer)

        Assertions.assertThat(actualBuffer).isEqualTo(buffer)
    }

    @Test
    fun readShouldFillEntityWithExactValuesFromBuffer() {
        val actualMessage = HeartBeatMessage::class.java.newInstance()

        actualMessage.read(buffer)

        Assertions.assertThat(actualMessage).isEqualToComparingFieldByFieldRecursively(message)
    }

    private fun createBuffer(value: String): ByteBuf = Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump((value)))

}