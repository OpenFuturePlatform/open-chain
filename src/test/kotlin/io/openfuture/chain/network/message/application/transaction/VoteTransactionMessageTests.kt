package io.openfuture.chain.network.message.application.transaction

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.openfuture.chain.config.MessageTests
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class VoteTransactionMessageTests : MessageTests() {

    private lateinit var message: VoteTransactionMessage
    private lateinit var buffer: ByteBuf


    @Before
    fun setup() {
        buffer = createBuffer("000000000000007b00000000000000010000000d73656e646572416464726573730000000f73656e646572507" +
            "5626c69634b65790000000f73656e6465725369676e61747572650000000468617368000000010000000b64656c65676174654b6579")
        message = VoteTransactionMessage(123, 1, "senderAddress", "senderPublicKey", "senderSignature", "hash", 1, "delegateKey")
    }

    @Test
    fun writeShouldWriteExactValuesInBuffer() {
        val actualBuffer = Unpooled.buffer()

        message.write(actualBuffer)

        assertThat(actualBuffer).isEqualTo(buffer)
    }

    @Test
    fun readShouldFillEntityWithExactValuesFromBuffer() {
        val actualMessage = VoteTransactionMessage::class.java.newInstance()

        actualMessage.read(buffer)

        assertThat(actualMessage).isEqualToComparingFieldByFieldRecursively(message)
    }

}