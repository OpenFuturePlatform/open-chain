package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.openfuture.chain.config.MessageTests
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class ConfirmedVoteTransactionMessageTests : MessageTests() {

    private lateinit var message: VoteTransactionMessage
    private lateinit var buffer: ByteBuf


    @Before
    fun setup() {
        buffer = createBuffer("000000000000007b00000000000000010000000d73656e6465724164647265737300000004686" +
            "173680000000f73656e6465725369676e61747572650000000f73656e6465725075626c69634b6579000000010000000b64656c6" +
            "5676174654b6579")
        message = VoteTransactionMessage(123, 1, "senderAddress", "hash", "senderSignature", "senderPublicKey", 1, "delegateKey")
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