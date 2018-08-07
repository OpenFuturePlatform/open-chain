package io.openfuture.chain.network.message.consensus

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.openfuture.chain.config.MessageTests
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class PendingBlockMessageTests : MessageTests() {

    private lateinit var message: PendingBlockMessage
    private lateinit var buffer: ByteBuf


    @Before
    fun setup() {
        buffer = createBuffer("00000000000000010000000870726576486173680000000000000001000000000000000a000000097075626c" +
            "69634b65790000000468617368000000097369676e61747572650000000a6d65726b6c654861736800000001000000056861736831")

        val transactions = mutableListOf("hash1")
        message = PendingBlockMessage(1, "prevHash", 1, 10, "hash", "signature", "publicKey", "merkleHash", transactions)
    }

    @Test
    fun writeShouldWriteExactValuesInBuffer() {
        val actualBuffer = Unpooled.buffer()

        message.write(actualBuffer)

        assertThat(actualBuffer).isEqualTo(buffer)
    }

    @Test
    fun readShouldFillEntityWithExactValuesFromBuffer() {
        val actualMessage = PendingBlockMessage::class.java.newInstance()

        actualMessage.read(buffer)

        assertThat(actualMessage).isEqualToComparingFieldByFieldRecursively(message)
    }

}