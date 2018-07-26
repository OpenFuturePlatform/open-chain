package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled
import io.openfuture.chain.domain.transaction.TransferTransactionDto
import io.openfuture.chain.domain.transaction.data.TransferTransactionData
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class NetworkTransferTransactionTest {

    private val buffer = Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump(
        "0000000000000001000000000000000100000010726563697069656e74416464726573730000000973656e6465724b6579000" +
            "00000000000010000000d73656e646572416464726573730000000f73656e6465725369676e61747572650000000468617368"))
    private val entity = TransferTransactionDto(TransferTransactionData(1, 1, "recipientAddress", "senderKey"), 1,
        "senderAddress", "senderSignature", "hash")

    @Test
    fun writeShouldWriteExactValuesInBuffer() {
        val actualBuffer = Unpooled.buffer()

        entity.write(actualBuffer)

        assertThat(actualBuffer).isEqualTo(buffer)
    }

//    @Test
//    fun readShouldFillEntityWithExactValuesFromBuffer() {
//        val actualEntity = TransferTransactionDto::class.java.newInstance()
//
//        actualEntity.read(buffer)
//
//        assertThat(actualEntity.hash).isEqualTo(entity.hash)
//    }

}