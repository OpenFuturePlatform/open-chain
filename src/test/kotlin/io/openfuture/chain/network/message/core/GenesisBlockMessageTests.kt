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
        buffer = createBuffer("00000000000000010000000c70726576696f757348617368000000000000000100000004686" +
            "17368000000097369676e6174757265000000097075626c69634b6579000000000000000100000001000000097075626c69634b6579")

        message = GenesisBlockMessage(1, "previousHash", 1, "hash",
            "signature", "publicKey", 1, listOf("publicKey"))
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