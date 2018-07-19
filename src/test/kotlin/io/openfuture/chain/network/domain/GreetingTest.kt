package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class GreetingTest {

    private val buffer = Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump(
        "000000093132372e302e302e3100002382"))
    private val entity = Greeting(NetworkAddress("127.0.0.1", 9090))

    @Test
    fun sendShouldWriteExactValuesInBuffer() {
        val actualBuffer = Unpooled.buffer()
        entity.send(actualBuffer)
        assertThat(actualBuffer).isEqualTo(buffer)
    }

    @Test
    fun getShouldFillEntityWithExactValuesFromBuffer() {
        val actualEntity = Greeting::class.java.newInstance()
        actualEntity.get(buffer)
        assertThat(actualEntity).isEqualTo(entity)
    }

}