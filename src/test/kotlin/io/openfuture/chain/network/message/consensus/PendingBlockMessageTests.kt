package io.openfuture.chain.network.message.consensus

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.openfuture.chain.config.MessageTests
import io.openfuture.chain.network.message.core.MainBlockMessage
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class PendingBlockMessageTests : MessageTests() {

    private lateinit var message: MainBlockMessage
    private lateinit var buffer: ByteBuf


    @Before
    fun setup() {
        buffer = createBuffer("00000000000000010000000870726576486173680000000000000001000000000000000a00000004" +
            "68617368000000097369676e6174757265000000097075626c69634b65790000000a6d65726b6c65486173680" +
            "00000010000000568617368310000000100000005686173683100000001000000056861736831")

        val transactions = mutableListOf("hash1")
        message = MainBlockMessage(1, "prevHash", 1, 10, "hash", "signature", "publicKey", "merkleHash", transactions,
            transactions, transactions)
    }

    @Test
    fun writeShouldWriteExactValuesInBuffer() {
        val actualBuffer = Unpooled.buffer()

        message.write(actualBuffer)

        assertThat(actualBuffer).isEqualTo(buffer)
    }

    @Test
    fun readShouldFillEntityWithExactValuesFromBuffer() {
        val actualMessage = MainBlockMessage::class.java.newInstance()

        actualMessage.read(buffer)

        assertThat(actualMessage).isEqualToComparingFieldByFieldRecursively(message)
    }

}