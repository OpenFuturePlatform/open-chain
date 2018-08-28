package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.openfuture.chain.config.MessageTests
import io.openfuture.chain.core.model.entity.transaction.confirmed.RewardTransaction
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class MainBlockMessageTests : MessageTests() {

    private lateinit var message: MainBlockMessage
    private lateinit var buffer: ByteBuf


    @Before
    fun setup() {
        buffer = createBuffer("00000000000000010000000870726576486173680000000000000001000000046861736800000009" +
            "7369676e6174757265000000097075626c69634b65790000000a6d65726b6c654861736800000000000000010000000000000000000" +
            "0000d73656e6465724164647265737300000004686173680000000f73656e6465725369676e61747572650000000f73656e64657250" +
            "75626c69634b6579000000000000000a00000010726563697069656e744164647265737300000001000000000000000100000000000" +
            "000010000000d73656e6465724164647265737300000004686173680000000f73656e6465725369676e61747572650000000f73656e" +
            "6465725075626c69634b6579000000010000000b64656c65676174654b6579000000010000000000000001000000000000000100000" +
            "00d73656e6465724164647265737300000004686173680000000f73656e6465725369676e61747572650000000f73656e6465725075" +
            "626c69634b65790000000b64656c65676174654b657900000004686f737400000001000000010000000000000001000000000000000" +
            "10000000d73656e6465724164647265737300000004686173680000000f73656e6465725369676e61747572650000000f73656e6465" +
            "725075626c69634b657900000000000000010000000b64656c65676174654b6579")

        val rewardTransaction = createRewardTransactionMessage(1L)
        val voteTransactionMessage = listOf(VoteTransactionMessage(1L, 1L, "senderAddress", "hash", "senderSignature",
            "senderPublicKey", 1, "delegateKey"))
        val transferTransactionMessage = listOf(TransferTransactionMessage(1L, 1L, "senderAddress", "hash", "senderSignature",
            "senderPublicKey", 1, "delegateKey"))
        val delegateTransactionMessage = listOf(DelegateTransactionMessage(1L, 1L, "senderAddress", "hash", "senderSignature",
            "senderPublicKey", "delegateKey", "host", 1))

        message = MainBlockMessage(1, "prevHash", 1, "hash", "signature", "publicKey", "merkleHash",
            rewardTransaction, voteTransactionMessage, delegateTransactionMessage, transferTransactionMessage)
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

    private fun createRewardTransactionMessage(timestamp: Long): RewardTransactionMessage =
        RewardTransactionMessage(timestamp, 0, "senderAddress", "hash",
            "senderSignature", "senderPublicKey", 10,
            "recipientAddress")

}