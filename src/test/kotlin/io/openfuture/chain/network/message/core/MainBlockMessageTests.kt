package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.openfuture.chain.config.MessageTests
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.block.payload.MainBlockPayload
import io.openfuture.chain.core.model.entity.transaction.confirmed.DelegateTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.RewardTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.TransferTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.VoteTransaction
import io.openfuture.chain.core.model.entity.transaction.payload.RewardTransactionPayload
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class MainBlockMessageTests : MessageTests() {

    private lateinit var message: MainBlockMessage
    private lateinit var buffer: ByteBuf


    @Before
    fun setup() {
        buffer = createBuffer("00000000000000010000000c70726576696f7573486173680000000000bc614e0000000468617" +
            "368000000097369676e6174757265000000097075626c69634b65790000000a6d65726b6c65486173680000000000bc614e00000" +
            "000000000000000000d73656e6465724164647265737300000004686173680000000f73656e6465725369676e61747572650000000" +
            "f73656e6465725075626c69634b6579000000000000000a00000010726563697069656e7441646472657373000000000000000000000000")
        val voteTransactions = listOf<VoteTransaction>()
        val transaferTransaction = listOf<TransferTransaction>()
        val delegateTransactions = listOf<DelegateTransaction>()
        val mainBlock = createMainBlock()
        val rewardTransaction = createRewardTransaction(mainBlock)
        message = MainBlockMessage(mainBlock, rewardTransaction, voteTransactions, delegateTransactions,
            transaferTransaction)
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

    private fun createMainBlock(): MainBlock =
        MainBlock(12345678, 1, "previousHash", "hash", "signature",
            "publicKey", MainBlockPayload("merkleHash"))

    private fun createRewardTransaction(block: MainBlock): RewardTransaction =
        RewardTransaction(12345678, 0, "senderAddress", "hash",
            "senderSignature", "senderPublicKey", block,
            RewardTransactionPayload(10, "recipientAddress"))

}