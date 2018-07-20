package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled
import org.assertj.core.api.Assertions
import org.junit.Test

class TimeTest {

    private val buffer = Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump(
        "0000000000000756b5b3000000001b34f908"))
    private val entity = Time(123123123, 456456456)

    @Test
    fun sendShouldWriteExactValuesInBuffer() {
        val actualBuffer = Unpooled.buffer()
        entity.send(actualBuffer)
        Assertions.assertThat(actualBuffer).isEqualTo(buffer)
    }

    @Test
    fun getShouldFillEntityWithExactValuesFromBuffer() {
        val actualEntity = Time::class.java.newInstance()
        actualEntity.get(buffer)
        Assertions.assertThat(actualEntity).isEqualTo(entity)
    }

}
