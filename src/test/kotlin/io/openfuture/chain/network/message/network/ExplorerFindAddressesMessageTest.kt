package io.openfuture.chain.network.message.network

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.openfuture.chain.config.MessageTests
import org.apache.commons.lang3.StringUtils.EMPTY
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class ExplorerFindAddressesMessageTest : MessageTests() {

    private lateinit var message: ExplorerFindAddressesMessage
    private lateinit var buffer: ByteBuf


    @Before
    fun setup() {
        buffer = createBuffer(EMPTY)
        message = ExplorerFindAddressesMessage()
    }

    @Test
    fun writeShouldWriteExactValuesInBuffer() {
        val actualBuffer = Unpooled.buffer()

        message.write(actualBuffer)

        assertThat(actualBuffer).isEqualTo(buffer)
    }

    @Test
    fun readShouldFillEntityWithExactValuesFromBuffer() {
        val actualMessage = ExplorerFindAddressesMessage::class.java.newInstance()

        actualMessage.read(buffer)

        assertThat(actualMessage).isEqualToComparingFieldByFieldRecursively(message)
    }

}