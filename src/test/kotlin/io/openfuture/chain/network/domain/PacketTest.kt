package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled
import io.openfuture.chain.network.domain.HeartBeat.Type.PING
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
    fun writeShouldWriteExactValuesInBufferAskTime() {
        val buf = createBuffer(
            "000500000005312e302e30000000000756b5b30000001caab5c3b3")
        val packet = AskTime(123123123123)

        read(packet, buf)
    }

    @Test
    fun readShouldFillEntityWithExactValuesFromBufferAskTime() {
        val buf = createBuffer(
            "000500000005312e302e30000000000756b5b30000001caab5c3b3")
        val packet = AskTime(123123123123)

        val actualPacket = write(packet, buf)

        assertThat(actualPacket).isEqualTo(packet)
    }

    @Test
    fun writeShouldWriteExactValuesInBufferTime() {
        val buf = createBuffer(
            "000600000005312e302e30000000000756b5b3000000000756b5b3000000001b34f908")
        val packet = Time(123123123, 456456456)

        read(packet, buf)
    }

    @Test
    fun readShouldFillEntityWithExactValuesFromBufferTime() {
        val buf = createBuffer(
            "000600000005312e302e30000000000756b5b3000000000756b5b3000000001b34f908")
        val packet = Time(123123123, 456456456)

        val actualPacket = write(packet, buf)

        assertThat(actualPacket).isEqualTo(packet)
    }

    @Test
    fun writeShouldWriteExactValuesInBufferAddress() {
        val buf = createBuffer(
            "000100000005312e302e30000000000756b5b3" +
                "00000002000000093132372e302e302e3100002382000000093132372e302e302e3100002383")
        val packet = Addresses(listOf(
            NetworkAddress("127.0.0.1", 9090),
            NetworkAddress("127.0.0.1", 9091)))

        read(packet, buf)
    }

    @Test
    fun readShouldFillEntityWithExactValuesFromBufferAddress() {
        val buf = createBuffer(
            "000100000005312e302e30000000000756b5b3" +
                "00000002000000093132372e302e302e3100002382000000093132372e302e302e3100002383")
        val packet = Addresses(listOf(
            NetworkAddress("127.0.0.1", 9090),
            NetworkAddress("127.0.0.1", 9091)))

        val actualPacket = write(packet, buf)

        assertThat(actualPacket).isEqualTo(packet)
    }

    @Test
    fun writeShouldWriteExactValuesInBufferFindAddress() {
        val buf = createBuffer(
            "000200000005312e302e30000000000756b5b3")
        val packet = FindAddresses()

        read(packet, buf)
    }

    @Test
    fun readShouldFillEntityWithExactValuesFromBufferFindAddress() {
        val buf = createBuffer(
            "000200000005312e302e30000000000756b5b3")
        val packet = FindAddresses()

        val actualPacket = write(packet, buf)

        assertThat(actualPacket.timestamp).isEqualTo(packet.timestamp)
    }

    @Test
    fun writeShouldWriteExactValuesInBufferGreeting() {
        val buf = createBuffer(
            "000300000005312e302e30000000000756b5b3000000093132372e302e302e3100002382")
        val packet = Greeting(NetworkAddress("127.0.0.1", 9090))

        read(packet, buf)
    }

    @Test
    fun readShouldFillEntityWithExactValuesFromBufferGreeting() {
        val buf = createBuffer(
            "000300000005312e302e30000000000756b5b3000000093132372e302e302e3100002382")
        val packet = Greeting(NetworkAddress("127.0.0.1", 9090))

        val actualPacket = write(packet, buf)

        assertThat(actualPacket).isEqualTo(packet)
    }

    @Test
    fun writeShouldWriteExactValuesInBufferHeartBeat() {
        val buf = createBuffer(
            "000400000005312e302e30000000000756b5b301")
        val packet = HeartBeat(PING)

        read(packet, buf)
    }

    @Test
    fun readShouldFillEntityWithExactValuesFromBufferHeartBeat() {
        val buf = createBuffer(
            "000400000005312e302e30000000000756b5b301")
        val packet = HeartBeat(PING)

        val actualPacket = write(packet, buf)

        assertThat(actualPacket).isEqualTo(packet)
    }

    @Test
    fun writeShouldWriteExactValuesInBufferBlockRequest() {
        val buf = createBuffer(
            "000700000005312e302e30000000000756b5b30000000468617368")
        val packet = NetworkBlockRequest("hash")

        read(packet, buf)
    }

    @Test
    fun readShouldFillEntityWithExactValuesFromBufferBlockRequest() {
        val buf = createBuffer(
            "000700000005312e302e30000000000756b5b30000000468617368")
        val packet = NetworkBlockRequest("hash")

        val actualPacket = write(packet, buf)

        assertThat(actualPacket).isEqualTo(packet)
    }

    @Test
    fun writeShouldWriteExactValuesInBufferGenesisBlock() {
        val buf = createBuffer(
            "000900000005312e302e30000000000756b5b300000000000000010000000c70726576696f757348617368000" +
                "0000a6d65726b6c65486173680000000000000001000000010000000468617368000000097369676e6174757265" +
                "000000000000000100000001000000096c6f63616c686f737400001f900000000a")
        val delegates = mutableSetOf(NetworkDelegate("localhost", 8080, 10))

        val packet = NetworkGenesisBlock(1, "previousHash", "merkleHash", 1, 1, "hash", "signature", 1, delegates)

        read(packet, buf)
    }

    @Test
    fun readShouldFillEntityWithExactValuesFromBufferGenesisBlock() {
        val buf = createBuffer(
            "000900000005312e302e30000000000756b5b300000000000000010000000c70726576696f757348617368000" +
                "0000a6d65726b6c65486173680000000000000001000000010000000468617368000000097369676e6174757265" +
                "000000000000000100000001000000096c6f63616c686f737400001f900000000a")
        val delegates = mutableSetOf(NetworkDelegate("localhost", 8080, 10))
        val packet = NetworkGenesisBlock(1, "previousHash", "merkleHash", 1, 1, "hash", "signature", 1, delegates)

        val actualPacket = write(packet, buf)

        assertThat((actualPacket as NetworkGenesisBlock).hash).isEqualTo(packet.hash)
    }

    @Test
    fun writeShouldWriteExactValuesInBufferMainBlock() {
        val buf = createBuffer(
            "000800000005312e302e30000000000756b5b300000000000000010000000c70726576696f75734861" +
                "73680000000a6d65726b6c65486173680000000000000001000000010000000468617368000000097369676e6174" +
                "7572650000000100000000000000013ff00000000000003ff000000000000000000010726563697069656e7441646" +
                "4726573730000000973656e6465724b65790000000d73656e646572416464726573730000000f73656e6465725369" +
                "676e617475726500000004686173680000000100000000000000013ff00000000000003ff0000000000000000000107" +
                "26563697069656e74416464726573730000000973656e6465724b65790000000d73656e64657241646472657373000000" +
                "0f73656e6465725369676e61747572650000000468617368000000010000000c64656c6567617465486f737400001f90")
        val transafetTransaction = mutableListOf(NetworkTransferTransaction(1, 1.0, 1.0, "recipientAddress",
            "senderKey", "senderAddress", "senderSignature", "hash"))
        val voteTransaction = mutableListOf(NetworkVoteTransaction(1, 1.0, 1.0, "recipientAddress", "senderKey",
            "senderAddress", "senderSignature", "hash", 1, "delegateHost", 8080))
        val packet = NetworkMainBlock(1, "previousHash", "merkleHash", 1, 1, "hash", "signature", transafetTransaction, voteTransaction)

        read(packet, buf)
    }

    @Test
    fun readShouldFillEntityWithExactValuesFromBufferMainBlock() {
        val buf = createBuffer(
            "000800000005312e302e30000000000756b5b300000000000000010000000c70726576696f75734861" +
                "73680000000a6d65726b6c65486173680000000000000001000000010000000468617368000000097369676e6174" +
                "7572650000000100000000000000013ff00000000000003ff000000000000000000010726563697069656e7441646" +
                "4726573730000000973656e6465724b65790000000d73656e646572416464726573730000000f73656e6465725369" +
                "676e617475726500000004686173680000000100000000000000013ff00000000000003ff0000000000000000000107" +
                "26563697069656e74416464726573730000000973656e6465724b65790000000d73656e64657241646472657373000000" +
                "0f73656e6465725369676e61747572650000000468617368000000010000000c64656c6567617465486f737400001f90")
        val transafetTransaction = mutableListOf(NetworkTransferTransaction(1, 1.0, 1.0, "recipientAddress",
            "senderKey", "senderAddress", "senderSignature", "hash"))
        val voteTransaction = mutableListOf(NetworkVoteTransaction(1, 1.0, 1.0, "recipientAddress", "senderKey",
            "senderAddress", "senderSignature", "hash", 1, "delegateHost", 8080))
        val packet = NetworkMainBlock(1, "previousHash", "merkleHash", 1, 1, "hash", "signature", transafetTransaction, voteTransaction)

        val actualPacket = write(packet, buf)

        assertThat((actualPacket as NetworkMainBlock).hash).isEqualTo(packet.hash)
    }

    private fun read(packet: Packet, buf: ByteBuf) {
        addFields(packet)

        val actualBuffer = Unpooled.buffer()
        Packet.Serializer().write(packet, actualBuffer)
        assertThat(actualBuffer).isEqualTo(buf)
    }

    private fun write(packet: Packet, buf: ByteBuf): Packet {
        addFields(packet)

        return Packet.Serializer().read(buf)
    }

    private fun createBuffer(value: String) : ByteBuf = Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump((value)))
    
    private fun addFields(packet: Packet) {
        packet.timestamp = 123123123
        packet.version = "1.0.0"
    }

}