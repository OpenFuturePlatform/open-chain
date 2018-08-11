package io.openfuture.chain.network.message.network

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.openfuture.chain.config.MessageTests
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class AskTimeMessageTests : MessageTests() {

    private lateinit var message: AskTimeMessage
    private lateinit var buffer: ByteBuf


    @Before
    fun setup() {
        buffer = createBuffer("0000001caab5c3b3")
        message = AskTimeMessage(123123123123)
    }

    @Test
    fun writeShouldWriteExactValuesInBuffer() {
        val actualBuffer = Unpooled.buffer()

        message.write(actualBuffer)

        assertThat(actualBuffer).isEqualTo(buffer)
    }

    @Test
    fun readShouldFillEntityWithExactValuesFromBuffer() {
        val actualMessage = AskTimeMessage::class.java.newInstance()

        actualMessage.read(buffer)

        assertThat(actualMessage).isEqualToComparingFieldByFieldRecursively(message)
    }

}