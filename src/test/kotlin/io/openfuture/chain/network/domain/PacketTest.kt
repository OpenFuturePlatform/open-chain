package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled
import io.openfuture.chain.annotation.NoArgConstructor
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PacketTest {

    @NoArgConstructor
    class TestPacket(version: String?, timestamp: Long?) : Packet(version, timestamp)

    @Test
    fun sendShouldWriteExactValuesInBufferIfFieldsAreNull() {
        val buffer = Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump(
            "0000"))
        val entity = TestPacket(null, null)

        val actualBuffer = Unpooled.buffer()
        entity.write(actualBuffer)

        assertThat(actualBuffer).isEqualTo(buffer)
    }

    @Test
    fun getShouldFillEntityWithExactValuesFromBufferIfFieldsAreNull() {
        val buffer = Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump(
            "0000"))

        val actualEntity = TestPacket::class.java.newInstance()
        actualEntity.read(buffer)

        assertThat(actualEntity.timestamp).isNull()
        assertThat(actualEntity.version).isNull()
    }


    @Test
    fun sendShouldWriteExactValuesInBufferIfFieldsNotNull() {
        val buffer = Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump(
            "0100000005312e302e3001000000000001e0f3"))
        val entity = TestPacket("1.0.0", 123123)

        val actualBuffer = Unpooled.buffer()
        entity.write(actualBuffer)

        assertThat(actualBuffer).isEqualTo(buffer)
    }

    @Test
    fun getShouldFillEntityWithExactValuesFromBufferIfFieldsNotNull() {
        val buffer = Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump(
            "0100000005312e302e3001000000000001e0f3"))

        val actualEntity = TestPacket::class.java.newInstance()
        actualEntity.read(buffer)

        assertThat(actualEntity.version).isEqualTo("1.0.0")
        assertThat(actualEntity.timestamp).isEqualTo(123123)
    }



}