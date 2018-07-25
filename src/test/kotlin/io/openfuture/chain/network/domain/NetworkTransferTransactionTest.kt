package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class NetworkTransferTransactionTest {

    private val buffer = Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump(
        "00000000000000013ff00000000000003ff000000000000000000010726563697069656e744164647265737300000" +
            "00973656e6465724b65790000000d73656e646572416464726573730000000f73656e6465725369676e61747572650000000468617368"))
    private val entity = NetworkTransferTransaction(1, 1.0, 1.0, "recipientAddress", "senderKey", "senderAddress", "senderSignature", "hash")

    @Test
    fun writeShouldWriteExactValuesInBuffer() {
        val actualBuffer = Unpooled.buffer()

        entity.write(actualBuffer)

        assertThat(actualBuffer).isEqualTo(buffer)
    }

    @Test
    fun readShouldFillEntityWithExactValuesFromBuffer() {
        val actualEntity = NetworkTransferTransaction::class.java.newInstance()

        actualEntity.read(buffer)

        assertThat(actualEntity.hash).isEqualTo(entity.hash)
    }

}