package io.openfuture.chain.network.message.application.delegate

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class NetworkDelegateTest {

    private lateinit var message: DelegateMessage
    private lateinit var buffer: ByteBuf


    @Before
    fun setup(){
        buffer = createBuffer("000000093132372e302e302e31000000036b6579")
        message = DelegateMessage("127.0.0.1", "key")
    }

    @Test
    fun writeShouldWriteExactValuesInBuffer() {
        val actualBuffer = Unpooled.buffer()

        message.write(actualBuffer)

        assertThat(actualBuffer).isEqualTo(buffer)
    }

    @Test
    fun readShouldFillEntityWithExactValuesFromBuffer() {
        val actualMessage = DelegateMessage::class.java.newInstance()

        actualMessage.read(buffer)

        assertThat(actualMessage).isEqualTo(message)
    }

    private fun createBuffer(value: String) : ByteBuf = Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump((value)))

}