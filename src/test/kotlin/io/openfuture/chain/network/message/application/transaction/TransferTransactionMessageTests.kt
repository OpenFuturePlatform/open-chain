package io.openfuture.chain.network.message.application.transaction

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.openfuture.chain.config.MessageTests
import io.openfuture.chain.network.message.core.TransferTransactionMessage
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class TransferTransactionMessageTests : MessageTests() {

    private lateinit var message: TransferTransactionMessage
    private lateinit var buffer: ByteBuf


    @Before
    fun setup() {
        buffer = createBuffer("000000000001e0f300000000000000010000000d73656e646572416464726573730000000f73656e646572507" +
            "5626c69634b65790000000f73656e6465725369676e61747572650000000468617368000000000000000a00000010726563697069656e7441646472657373")
        message = TransferTransactionMessage(123123, 1, "senderAddress", "senderPublicKey", "senderSignature", "hash",
            10, "recipientAddress")
    }

    @Test
    fun writeShouldWriteExactValuesInBuffer() {
        val actualBuffer = Unpooled.buffer()

        message.write(actualBuffer)

        assertThat(actualBuffer).isEqualTo(buffer)
    }

    @Test
    fun readShouldFillEntityWithExactValuesFromBuffer() {
        val actualMessage = TransferTransactionMessage::class.java.newInstance()

        actualMessage.read(buffer)

        assertThat(actualMessage).isEqualToComparingFieldByFieldRecursively(message)
    }

}