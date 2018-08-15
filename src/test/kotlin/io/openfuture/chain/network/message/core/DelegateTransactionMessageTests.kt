package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.openfuture.chain.config.MessageTests
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class DelegateTransactionMessageTests : MessageTests() {

    private lateinit var message: DelegateTransactionMessage
    private lateinit var buffer: ByteBuf


    @Before
    fun setup() {
        buffer = createBuffer("000000000001e0f300000000000000010000000d73656e646572416464726573730000000468617" +
            "3680000000f73656e6465725369676e61747572650000000f73656e6465725075626c69634b65790000000b64656c65676174654b6" +
            "57900000004686f737400000001")
        message = DelegateTransactionMessage(123123, 1, "senderAddress", "hash",
            "senderSignature", "senderPublicKey", "delegateKey", "host", 1)
    }

    @Test
    fun writeShouldWriteExactValuesInBuffer() {
        val actualBuffer = Unpooled.buffer()

        message.write(actualBuffer)

        assertThat(actualBuffer).isEqualTo(buffer)
    }

    @Test
    fun readShouldFillEntityWithExactValuesFromBuffer() {
        val actualMessage = DelegateTransactionMessage::class.java.newInstance()

        actualMessage.read(buffer)

        assertThat(actualMessage).isEqualToComparingFieldByFieldRecursively(message)
    }

}