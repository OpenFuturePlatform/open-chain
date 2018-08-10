package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.openfuture.chain.config.MessageTests
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class TransferTransactionMessageTests : MessageTests() {

    private lateinit var message: TransferTransactionMessage
    private lateinit var buffer: ByteBuf


    @Before
    fun setup() {
        buffer = createBuffer("000000000001e0f300000000000000010000000d73656e64657241646472657373000000046861" +
            "73680000000f73656e6465725369676e61747572650000000f73656e6465725075626c69634b6579000000000000000a00000010" +
            "726563697069656e7441646472657373")
        message = TransferTransactionMessage(123123, 1, "senderAddress", "hash",
            "senderSignature", "senderPublicKey", 10, "recipientAddress")
    }

    @Test
    fun writeShouldWriteExactValuesInBuffer() {
        val actualBuffer = Unpooled.buffer()

        message.write(actualBuffer)

        assertThat(actualBuffer).isEqualTo(buffer)
    }

    @Test
    fun readShouldFillEntityWithExactValuesFromBuffer() {
        val actualMessage = TransferTransactionMessage::class.java.getConstructor().newInstance()

        actualMessage.read(buffer)

        assertThat(actualMessage).isEqualToComparingFieldByFieldRecursively(message)
    }

}