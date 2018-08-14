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
        buffer = createBuffer("0000000000000001000000087072657648617368000000000000000100000004686173680000000" +
            "97369676e6174757265000000097075626c69634b65790000000a6d65726b6c65486173680000000100000005686173683100000" +
            "00100000005686173683100000001000000056861736831")

        val transactions = mutableListOf("hash1")
        message = PendingBlockMessage(1, "prevHash", 1, "hash", "signature",
            "publicKey", "merkleHash", transactions, transactions, transactions)
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