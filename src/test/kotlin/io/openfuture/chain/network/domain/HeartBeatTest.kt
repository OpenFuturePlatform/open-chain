package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled
import org.assertj.core.api.Assertions
import org.junit.Test

class HeartBeatTest {

    private val buffer =  Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump("01"))
    private val entity = HeartBeat(HeartBeat.Type.PING)

    @Test
    fun sendShouldWriteExactValuesInBuffer() {
        val actualBuffer = Unpooled.buffer()
        entity.send(actualBuffer)
        Assertions.assertThat(actualBuffer).isEqualTo(buffer)
    }

    @Test
    fun getShouldFillEntityWithExactValuesFromBuffer() {
        val actualEntity = HeartBeat()
        actualEntity.get(buffer)
        Assertions.assertThat(actualEntity).isEqualTo(entity)
    }

}
