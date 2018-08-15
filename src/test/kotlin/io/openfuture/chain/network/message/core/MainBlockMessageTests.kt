package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.openfuture.chain.config.MessageTests
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class MainBlockMessageTests : MessageTests() {

    private lateinit var message: MainBlockMessage
    private lateinit var buffer: ByteBuf


    @Before
    fun setup() {
        buffer = createBuffer("00000000000000010000000870726576486173680000000000000001000000000000000a00000004" +
            "68617368000000097369676e6174757265000000097075626c69634b65790000000a6d65726b6c65486173680000000100000000000" +
            "0000100000000000000010000000d73656e6465724164647265737300000004686173680000000f73656e6465725369676e61747572" +
            "650000000f73656e6465725075626c69634b6579000000010000000b64656c65676174654b657900000001000000000000000100000" +
            "000000000010000000d73656e6465724164647265737300000004686173680000000f73656e6465725369676e61747572650000000f" +
            "73656e6465725075626c69634b65790000000b64656c65676174654b657900000004686f73740000000100000001000000000000000" +
            "100000000000000010000000d73656e6465724164647265737300000004686173680000000f73656e6465725369676e617475726500" +
            "00000f73656e6465725075626c69634b657900000000000000010000000b64656c65676174654b6579")
        val voteTransactionMessage = listOf(VoteTransactionMessage(1L, 1L, "senderAddress", "hash", "senderSignature",
            "senderPublicKey", 1, "delegateKey"))
        val transferTransactionMessage = listOf(TransferTransactionMessage(1L, 1L, "senderAddress", "hash", "senderSignature",
            "senderPublicKey", 1, "delegateKey"))
        val delegateTransactionMessage = listOf(DelegateTransactionMessage(1L, 1L, "senderAddress", "hash", "senderSignature",
            "senderPublicKey", "delegateKey", "host", 1))

        message = MainBlockMessage(1, "prevHash", 1, 10, "hash", "signature", "publicKey", "merkleHash",
            voteTransactionMessage, delegateTransactionMessage, transferTransactionMessage)
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