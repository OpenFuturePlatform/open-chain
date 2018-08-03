package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.openfuture.chain.config.MessageTests
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class GenesisBlockMessageTests : MessageTests() {

    private lateinit var message: GenesisBlockMessage
    private lateinit var buffer: ByteBuf


    @Before
    fun setup() {
        buffer = createBuffer("00000000000000010000000c70726576696f7573486173680000000000000001000000000000000a000000097" +
            "075626c69634b65790000000468617368000000097369676e6174757265000000000000000100000001000000096c6f63616c686f73" +
            "74000000036b6579")

        val delegates = mutableSetOf(DelegateMessage("localhost", "key"))
        message = GenesisBlockMessage(1, "previousHash", 1, 10, "publicKey", "hash", "signature", 1, delegates)
    }

    @Test
    fun writeShouldWriteExactValuesInBuffer() {
        val actualBuffer = Unpooled.buffer()

        message.write(actualBuffer)

        assertThat(actualBuffer).isEqualTo(buffer)
    }

    @Test
    fun readShouldFillEntityWithExactValuesFromBuffer() {
        val actualMessage = GenesisBlockMessage::class.java.newInstance()

        actualMessage.read(buffer)

        assertThat(actualMessage).isEqualToComparingFieldByFieldRecursively(message)
    }

}