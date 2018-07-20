package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled
import org.assertj.core.api.Assertions
import org.junit.Test

class AskTimeTest {

    private val buffer = Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump("00000000001caab5c3b3"))
    private val entity = AskTime(123123123123)

    @Test
    fun sendShouldWriteExactValuesInBuffer() {
        val actualBuffer = Unpooled.buffer()
        entity.write(actualBuffer)
        Assertions.assertThat(actualBuffer).isEqualTo(buffer)
    }

    @Test
    fun getShouldFillEntityWithExactValuesFromBuffer() {
        val actualEntity = AskTime::class.java.newInstance()
        actualEntity.read(buffer)
        Assertions.assertThat(actualEntity).isEqualTo(entity)
    }

}