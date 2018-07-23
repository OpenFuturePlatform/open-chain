package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled
import io.openfuture.chain.network.domain.HeartBeat.Type.PING
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PacketTest {

    @Test(expected = IllegalStateException::class)
    fun writeShouldThrowExceptionIfCommonRequiredFieldsAreNull() {
        val entity = FindAddresses()

        val actualBuffer = Unpooled.buffer()
        Packet.Serializer().write(entity, actualBuffer)
    }

    @Test
    fun writeShouldWorkWithAddressesPacket() {
        val buffer = Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump(
            "000100000005312e302e30000000000756b5b3" +
                "00000002000000093132372e302e302e3100002382000000093132372e302e302e3100002383"))
        val entity = Addresses(listOf(
            NetworkAddress("127.0.0.1", 9090),
            NetworkAddress("127.0.0.1", 9091)))
        addBaseFields(entity)

        val actualBuffer = Unpooled.buffer()
        Packet.Serializer().write(entity, actualBuffer)
        assertThat(actualBuffer).isEqualTo(buffer)
    }

    @Test
    fun readShouldWorkWithAddressesPacket() {
        val buffer = Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump(
            "000100000005312e302e30000000000756b5b3" +
                "00000002000000093132372e302e302e3100002382000000093132372e302e302e3100002383"))
        val entity = Addresses(listOf(
            NetworkAddress("127.0.0.1", 9090),
            NetworkAddress("127.0.0.1", 9091)))
        addBaseFields(entity)

        val actualEntity = Packet.Serializer().read(buffer)
        assertThat(actualEntity).isEqualTo(entity)
    }

    @Test
    fun writeShouldWorkWithFindAddressesPacket() {
        val buffer = Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump(
            "000200000005312e302e30000000000756b5b3"))
        val entity = FindAddresses()
        addBaseFields(entity)

        val actualBuffer = Unpooled.buffer()
        Packet.Serializer().write(entity, actualBuffer)

        assertThat(actualBuffer).isEqualTo(buffer)
    }

    @Test
    fun readShouldWorkWithFindAddressesPacket() {
        val buffer = Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump(
            "000200000005312e302e30000000000756b5b3"))

        val actualEntity = Packet.Serializer().read(buffer)

        assertThat(actualEntity.version).isEqualTo("1.0.0")
        assertThat(actualEntity.timestamp).isEqualTo(123123123)
    }

    @Test
    fun readShouldWorkWithGreetingPacket() {
        val buffer = Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump(
            "000300000005312e302e30000000000756b5b3000000093132372e302e302e3100002382"))
        val entity = Greeting(NetworkAddress("127.0.0.1", 9090))
        addBaseFields(entity)

        val actualBuffer = Unpooled.buffer()
        Packet.Serializer().write(entity, actualBuffer)
        assertThat(actualBuffer).isEqualTo(buffer)
    }

    @Test
    fun writeShouldWorkWithGreetingPacket() {
        val buffer = Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump(
            "000300000005312e302e30000000000756b5b3000000093132372e302e302e3100002382"))
        val entity = Greeting(NetworkAddress("127.0.0.1", 9090))
        addBaseFields(entity)


        val actualEntity = Packet.Serializer().read(buffer)
        assertThat(actualEntity).isEqualTo(entity)
    }

    @Test
    fun readShouldWorkWithHeartBeatPacket() {
        val buffer = Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump(
            "000400000005312e302e30000000000756b5b301"))
        val entity = HeartBeat(PING)
        addBaseFields(entity)

        val actualBuffer = Unpooled.buffer()
        Packet.Serializer().write(entity, actualBuffer)
        Assertions.assertThat(actualBuffer).isEqualTo(buffer)
    }

    @Test
    fun writeShouldWorkWithHeartBeatPacket() {
        val buffer = Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump(
            "000400000005312e302e30000000000756b5b301"))
        val entity = HeartBeat(PING)
        addBaseFields(entity)

        val actualEntity = Packet.Serializer().read(buffer)
        Assertions.assertThat(actualEntity).isEqualTo(entity)
    }

    @Test
    fun writeShouldWorkWithAskTimePacket() {
        val buffer = Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump(
            "000500000005312e302e30000000000756b5b30000001caab5c3b3"))
        val entity = AskTime(123123123123)
        addBaseFields(entity)

        val actualBuffer = Unpooled.buffer()
        Packet.Serializer().write(entity, actualBuffer)
        Assertions.assertThat(actualBuffer).isEqualTo(buffer)
    }

    @Test
    fun readShouldWorkWithAskTimePacket() {
        val buffer = Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump(
            "000500000005312e302e30000000000756b5b30000001caab5c3b3"))
        val entity = AskTime(123123123123)
        addBaseFields(entity)

        val actualEntity = Packet.Serializer().read(buffer)
        Assertions.assertThat(actualEntity).isEqualTo(entity)
    }

    @Test
    fun readShouldWorkWithTimePacket() {
        val buffer = Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump(
            "000600000005312e302e30000000000756b5b3000000000756b5b3000000001b34f908"))
        val entity = Time(123123123, 456456456)
        addBaseFields(entity)

        val actualBuffer = Unpooled.buffer()
        Packet.Serializer().write(entity, actualBuffer)
        Assertions.assertThat(actualBuffer).isEqualTo(buffer)
    }

    @Test
    fun writeShouldWorkWithTimePacket() {
        val buffer = Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump(
            "000600000005312e302e30000000000756b5b3000000000756b5b3000000001b34f908"))
        val entity = Time(123123123, 456456456)
        addBaseFields(entity)

        val actualEntity = Packet.Serializer().read(buffer)
        Assertions.assertThat(actualEntity).isEqualTo(entity)
    }

    private fun addBaseFields(entity: Packet) {
        entity.timestamp = 123123123
        entity.version = "1.0.0"
    }

}