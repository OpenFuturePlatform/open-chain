package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class FindAddressesTest {

    private val buffer = Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump("0000"))
    private val entity = FindAddresses()

    @Test
    fun sendShouldWriteExactValuesInBuffer() {
        val actualBuffer = Unpooled.buffer()
        entity.send(actualBuffer)
        assertThat(actualBuffer).isEqualTo(buffer)
    }

    @Test
    fun getShouldFillEntityWithExactValuesFromBuffer() {
        val actualEntity = FindAddresses()
        actualEntity.get(buffer)
        assertThat(actualEntity).isEqualTo(entity)
    }

}