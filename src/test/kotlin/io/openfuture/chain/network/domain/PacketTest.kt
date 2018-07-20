package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled
import io.openfuture.chain.annotation.NoArgConstructor
import io.openfuture.chain.network.domain.HeartBeat.*
import org.junit.Test
import org.assertj.core.api.Assertions.assertThat

class PacketTest {

    @NoArgConstructor
    class TestPacket(version: String?, timestamp: Long?) : Packet(version, timestamp)

    @Test
    fun sendShouldWriteExactValuesInBufferIfFieldsAreNull() {
        val buffer = Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump(
            "0000"))
        val entity = TestPacket(null, null)

        val actualBuffer = Unpooled.buffer()
        entity.send(actualBuffer)

        assertThat(actualBuffer).isEqualTo(buffer)
    }

    @Test
    fun getShouldFillEntityWithExactValuesFromBufferIfFieldsAreNull() {
        val buffer = Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump(
            "0000"))

        val actualEntity = TestPacket::class.java.newInstance()
        actualEntity.get(buffer)

        assertThat(actualEntity.timestamp).isNull()
        assertThat(actualEntity.version).isNull()
    }


    @Test
    fun sendShouldWriteExactValuesInBufferIfFieldsNotNull() {
        val buffer = Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump(
            "0100000005312e302e3001000000000001e0f3"))
        val entity = TestPacket("1.0.0", 123123)

        val actualBuffer = Unpooled.buffer()
        entity.send(actualBuffer)

        assertThat(actualBuffer).isEqualTo(buffer)
    }

    @Test
    fun getShouldFillEntityWithExactValuesFromBufferIfFieldsNotNull() {
        val buffer = Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump(
            "0100000005312e302e3001000000000001e0f3"))

        val actualEntity = TestPacket::class.java.newInstance()
        actualEntity.get(buffer)

        assertThat(actualEntity.version).isEqualTo("1.0.0")
        assertThat(actualEntity.timestamp).isEqualTo(123123)
    }

    @Test
    fun writeShouldAddExactIdToByteBuffer() {
        val addresses = Addresses(listOf(NetworkAddress("localhost",9090)))
        val findAddresses = FindAddresses()
        val greeting = Greeting(NetworkAddress("localhost",9090))
        val heartBeat = HeartBeat(Type.PING)
        val findTime = AskTime(123123)
        val time = Time(123123,123456)

        val expectedBuffer = Unpooled.buffer()
        expectedBuffer.writeShort(1)
        addresses.send(expectedBuffer)
        expectedBuffer.writeShort(2)
        findAddresses.send(expectedBuffer)
        expectedBuffer.writeShort(3)
        greeting.send(expectedBuffer)
        expectedBuffer.writeShort(4)
        heartBeat.send(expectedBuffer)
        expectedBuffer.writeShort(5)
        findTime.send(expectedBuffer)
        expectedBuffer.writeShort(6)
        time.send(expectedBuffer)

        val actualBuffer = Unpooled.buffer()
        Packet.write(addresses, actualBuffer)
        Packet.write(findAddresses, actualBuffer)
        Packet.write(greeting, actualBuffer)
        Packet.write(heartBeat, actualBuffer)
        Packet.write(findTime, actualBuffer)
        Packet.write(time, actualBuffer)

        assertThat(actualBuffer).isEqualTo(expectedBuffer)
    }

    @Test
    fun readShouldUseExactIdToIdentifyPackets() {
        val addresses = Addresses(listOf(NetworkAddress("localhost",9090)))
        val findAddresses = FindAddresses()
        val greeting = Greeting(NetworkAddress("localhost",9090))
        val heartBeat = HeartBeat(Type.PING)
        val findTime = AskTime(123123)
        val time = Time(123123,123456)

        val buffer = Unpooled.buffer()
        buffer.writeShort(1)
        addresses.send(buffer)
        buffer.writeShort(2)
        findAddresses.send(buffer)
        buffer.writeShort(3)
        greeting.send(buffer)
        buffer.writeShort(4)
        heartBeat.send(buffer)
        buffer.writeShort(5)
        findTime.send(buffer)
        buffer.writeShort(6)
        time.send(buffer)

        assertThat(Packet.read(buffer)).isInstanceOf(Addresses::class.java)
        assertThat(Packet.read(buffer)).isInstanceOf(FindAddresses::class.java)
        assertThat(Packet.read(buffer)).isInstanceOf(Greeting::class.java)
        assertThat(Packet.read(buffer)).isInstanceOf(HeartBeat::class.java)
        assertThat(Packet.read(buffer)).isInstanceOf(AskTime::class.java)
        assertThat(Packet.read(buffer)).isInstanceOf(Time::class.java)
    }

}