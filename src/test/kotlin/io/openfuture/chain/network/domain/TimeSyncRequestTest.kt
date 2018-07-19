package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled
import org.assertj.core.api.Assertions
import org.junit.Test

class TimeSyncRequestTest {

    private val buffer = Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump("0000001caab5c3b3"))
    private val entity = TimeSyncRequest(123123123123)

    @Test
    fun sendShouldWriteExactValuesInBuffer() {
        val actualBuffer = Unpooled.buffer()
        entity.send(actualBuffer)
        Assertions.assertThat(actualBuffer).isEqualTo(buffer)
    }

    @Test
    fun getShouldFillEntityWithExactValuesFromBuffer() {
        val actualEntity = TimeSyncRequest::class.java.newInstance()
        actualEntity.get(buffer)
        Assertions.assertThat(actualEntity).isEqualTo(entity)
    }

}