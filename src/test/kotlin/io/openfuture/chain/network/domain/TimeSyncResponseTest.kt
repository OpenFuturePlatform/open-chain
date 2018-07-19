package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled
import org.assertj.core.api.Assertions
import org.junit.Test

class TimeSyncResponseTest {

    private val buffer = Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump(
        "000000000756b5b3000000001b34f908"))
    private val entity = TimeSyncResponse(123123123, 456456456)

    @Test
    fun sendShouldWriteExactValuesInBuffer() {
        val actualBuffer = Unpooled.buffer()
        entity.send(actualBuffer)
        Assertions.assertThat(actualBuffer).isEqualTo(buffer)
    }

    @Test
    fun getShouldFillEntityWithExactValuesFromBuffer() {
        val actualEntity = TimeSyncResponse::class.java.newInstance()
        actualEntity.get(buffer)
        Assertions.assertThat(actualEntity).isEqualTo(entity)
    }

}
