package io.openfuture.chain.network.message.consensus

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.openfuture.chain.config.MessageTests
import io.openfuture.chain.network.message.core.RewardTransactionMessage
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class PendingBlockMessageTests : MessageTests() {

    private lateinit var message: PendingBlockMessage
    private lateinit var buffer: ByteBuf


    @Before
    fun setup() {
        buffer = createBuffer("00000000000000010000000870726576486173680000000000000001000000046861736800000009" +
            "7369676e6174757265000000097075626c69634b65790000000a6d65726b6c654861736800000000000000010000000000000000000" +
            "0000d73656e6465724164647265737300000004686173680000000f73656e6465725369676e61747572650000000f73656e64657250" +
            "75626c69634b6579000000000000000a00000010726563697069656e7441646472657373000000000000000000000000")

        val rewardTransactionMessage = createRewardTransactionMessage(1L)
        message = PendingBlockMessage(1, "prevHash", 1L, "hash", "signature",
            "publicKey", "merkleHash", rewardTransactionMessage, listOf(), listOf(), listOf())
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

    private fun createRewardTransactionMessage(timestamp: Long): RewardTransactionMessage =
        RewardTransactionMessage(timestamp, 0, "senderAddress", "hash",
            "senderSignature", "senderPublicKey", 10,
            "recipientAddress")

}