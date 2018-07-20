package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled
import org.assertj.core.api.Assertions
import org.junit.Test

class HeartBeatTest {

    private val buffer = Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump("000001"))
    private val entity = HeartBeat(HeartBeat.Type.PING)

    @Test
    fun sendShouldWriteExactValuesInBuffer() {
        val actualBuffer = Unpooled.buffer()
        entity.write(actualBuffer)
        Assertions.assertThat(actualBuffer).isEqualTo(buffer)
    }

    @Test
    fun getShouldFillEntityWithExactValuesFromBuffer() {
        val actualEntity = HeartBeat::class.java.newInstance()
        actualEntity.read(buffer)
        Assertions.assertThat(actualEntity).isEqualTo(entity)
    }

}
