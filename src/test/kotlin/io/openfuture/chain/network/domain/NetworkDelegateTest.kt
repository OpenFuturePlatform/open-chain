package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class NetworkDelegateTest {

    private val buffer = Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump(
        "000000093132372e302e302e31000023820000000a"))
    private val entity = NetworkDelegate("127.0.0.1", 9090, 10)

    @Test
    fun writeShouldWriteExactValuesInBuffer() {
        val actualBuffer = Unpooled.buffer()

        entity.write(actualBuffer)

        assertThat(actualBuffer).isEqualTo(buffer)
    }

    @Test
    fun readShouldFillEntityWithExactValuesFromBuffer() {
        val actualEntity = NetworkDelegate::class.java.newInstance()

        actualEntity.read(buffer)

        assertThat(actualEntity).isEqualTo(entity)
    }

}