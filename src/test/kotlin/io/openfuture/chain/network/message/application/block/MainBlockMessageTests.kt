package io.openfuture.chain.network.message.application.block

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.openfuture.chain.config.MessageTests
import io.openfuture.chain.network.message.application.transaction.DelegateTransactionMessage
import io.openfuture.chain.network.message.application.transaction.TransferTransactionMessage
import io.openfuture.chain.network.message.application.transaction.VoteTransactionMessage
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class MainBlockMessageTests : MessageTests() {

    private lateinit var message: MainBlockMessage
    private lateinit var buffer: ByteBuf


    @Before
    fun setup() {
        buffer = createBuffer("00000000000000010000000c70726576696f7573486173680000000000000001000000000000000a000000097" +
            "075626c69634b65790000000468617368000000097369676e61747572650000000a6d65726b6c654861736800000001000000000001" +
            "e0f300000000000000010000000d73656e646572416464726573730000000f73656e6465725075626c69634b65790000000f73656e6" +
            "465725369676e61747572650000000468617368000000000000000a00000010726563697069656e7441646472657373000000010000" +
            "00000000007b00000000000000010000000d73656e646572416464726573730000000f73656e6465725075626c69634b65790000000" +
            "f73656e6465725369676e61747572650000000468617368000000010000000b64656c65676174654b657900000001000000000001e0" +
            "f300000000000000010000000d73656e646572416464726573730000000f73656e6465725075626c69634b65790000000f73656e646" +
            "5725369676e617475726500000004686173680000000b64656c65676174654b6579")

        val transferTransaction = mutableListOf(TransferTransactionMessage(123123, 1, "senderAddress", "senderPublicKey",
            "senderSignature", "hash", 10, "recipientAddress"))
        val voteTransaction = mutableListOf(VoteTransactionMessage(123, 1, "senderAddress", "senderPublicKey",
            "senderSignature", "hash", 1, "delegateKey"))
        val delegateTransaction = mutableListOf(DelegateTransactionMessage(123123, 1, "senderAddress", "senderPublicKey",
            "senderSignature", "hash", "delegateKey"))
        message = MainBlockMessage(1, "previousHash", 1, 10, "hash", "signature", "publicKey", "merkleHash",
            transferTransaction, voteTransaction, delegateTransaction)
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