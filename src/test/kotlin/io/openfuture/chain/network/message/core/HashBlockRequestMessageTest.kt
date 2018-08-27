package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.openfuture.chain.config.MessageTests
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test

class HashBlockRequestMessageTest : MessageTests() {

    private lateinit var message: SyncBlockRequestMessage
    private lateinit var buffer: ByteBuf


    @Before
    fun setup() {
        buffer = createBuffer("0000000468617368")
        message = SyncBlockRequestMessage("hash")
    }

    @Test
    fun writeShouldWriteExactValuesInBuffer() {
        val actualBuffer = Unpooled.buffer()

        message.write(actualBuffer)

        Assertions.assertThat(actualBuffer).isEqualTo(buffer)
    }

    @Test
    fun readShouldFillEntityWithExactValuesFromBuffer() {
        val actualMessage = SyncBlockRequestMessage::class.java.newInstance()

        actualMessage.read(buffer)

        Assertions.assertThat(actualMessage).isEqualToComparingFieldByFieldRecursively(message)
    }

}