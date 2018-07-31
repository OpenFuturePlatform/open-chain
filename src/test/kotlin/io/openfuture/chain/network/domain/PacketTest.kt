package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled
import io.openfuture.chain.block.BlockApprovalMessage
import io.openfuture.chain.consensus.component.block.ObserverStage
import io.openfuture.chain.consensus.model.dto.transaction.DelegateTransactionDto
import io.openfuture.chain.consensus.model.dto.transaction.RewardTransactionDto
import io.openfuture.chain.consensus.model.dto.transaction.TransferTransactionDto
import io.openfuture.chain.consensus.model.dto.transaction.VoteTransactionDto
import io.openfuture.chain.consensus.model.dto.transaction.data.DelegateTransactionData
import io.openfuture.chain.consensus.model.dto.transaction.data.RewardTransactionData
import io.openfuture.chain.consensus.model.dto.transaction.data.TransferTransactionData
import io.openfuture.chain.consensus.model.dto.transaction.data.VoteTransactionData
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
        val buf = createBuffer("000900000005312e302e30000000000756b5b300000000000000010000000c70726576696f75734861736" +
                "800000000000000010000000468617368000000097369676e6174757265000000036b6579000000000000000100000001000" +
                "000096c6f63616c686f7374000000036b6579")
        val delegates = mutableSetOf(NetworkDelegate("localhost", "key"))

        val packet = NetworkGenesisBlock(1, "previousHash", 1, "hash", "signature", "key", 1, delegates)

        read(packet, buf)
    }

    @Test
    fun readShouldFillEntityWithExactValuesFromBufferGenesisBlock() {
        val buf = createBuffer("000900000005312e302e30000000000756b5b300000000000000010000000c70726576696f75734861736" +
                "800000000000000010000000468617368000000097369676e6174757265000000036b6579000000000000000100000001000" +
                "000096c6f63616c686f7374000000036b6579")
        val delegates = mutableSetOf(NetworkDelegate("localhost", "key"))
        val packet = NetworkGenesisBlock(1, "previousHash", 1, "hash", "signature", "key", 1, delegates)

        val actualPacket = write(packet, buf)

        assertThat((actualPacket as NetworkGenesisBlock).hash).isEqualTo(packet.hash)
    }

    @Test
    fun writeShouldWriteExactValuesInBufferMainBlock() {
        val buf = createBuffer("000800000005312e302e30000000000756b5b300000000000000010000000c70726576696f75734861736800000" +
                "00000000001000000097369676e61747572650000000a6d65726b6c65486173680000000468617368000000036b657" +
                "9000000010000000000000001000000000000000100000010726563697069656e74416464726573730000000973656e64" +
                "65724b657900000000000000010000000d73656e646572416464726573730000000f73656e6465725369676e6174757265" +
                "0000000468617368000000010000000000000001000000000000000100000010726563697069656e7441646472657373000" +
                "0000d73656e6465724164647265737300000001000000036b657900000000000000010000000973656e6465724b657900000" +
                "00f73656e6465725369676e61747572650000000468617368000000010000000000000001000000000000000100000010726" +
                "563697069656e74416464726573730000000973656e6465724b65790000000b64656c65676174654b6579000000000000000100" +
                "00000d73656e646572416464726573730000000f73656e6465725369676e6174757265000000046861736800000001000000000" +
                "0000001000000000000000100000010726563697069656e74416464726573730000000973656e6465724b657900000000000000" +
                "010000000d73656e646572416464726573730000000f73656e6465725369676e61747572650000000468617368")
        val transaferTransaction = mutableListOf(TransferTransactionDto(TransferTransactionData(1, 1, "recipientAddress",
            "senderKey"), 1, "senderAddress", "senderSignature", "hash"))
        val voteTransaction = mutableListOf(VoteTransactionDto(VoteTransactionData(1, 1, "recipientAddress", "senderAddress",
            1, "key"), 1, "senderKey", "senderSignature", "hash"))
        val delegateTransaction = mutableListOf(DelegateTransactionDto(DelegateTransactionData(1, 1, "recipientAddress",
            "senderKey", "delegateKey"), 1, "senderAddress", "senderSignature", "hash"))
        val rewardTransaction = mutableListOf(RewardTransactionDto(RewardTransactionData(1, 1, "recipientAddress",
            "senderKey"), 1, "senderAddress", "senderSignature", "hash"))
        val packet = NetworkMainBlock(1, "previousHash", 1, "merkleHash", "hash", "signature", "key", transaferTransaction,
            voteTransaction, delegateTransaction, rewardTransaction)

        read(packet, buf)
    }

    @Test
    fun readShouldFillEntityWithExactValuesFromBufferMainBlock() {
        val buf = createBuffer("000800000005312e302e30000000000756b5b300000000000000010000000c70726576696f75734861736800000" +
                "00000000001000000097369676e61747572650000000a6d65726b6c65486173680000000468617368000000036b657" +
                "9000000010000000000000001000000000000000100000010726563697069656e74416464726573730000000973656e64" +
                "65724b657900000000000000010000000d73656e646572416464726573730000000f73656e6465725369676e6174757265" +
                "0000000468617368000000010000000000000001000000000000000100000010726563697069656e7441646472657373000" +
                "0000d73656e6465724164647265737300000001000000036b657900000000000000010000000973656e6465724b657900000" +
                "00f73656e6465725369676e61747572650000000468617368000000010000000000000001000000000000000100000010726" +
                "563697069656e74416464726573730000000973656e6465724b65790000000b64656c65676174654b6579000000000000000100" +
                "00000d73656e646572416464726573730000000f73656e6465725369676e6174757265000000046861736800000001000000000" +
                "0000001000000000000000100000010726563697069656e74416464726573730000000973656e6465724b657900000000000000" +
                "010000000d73656e646572416464726573730000000f73656e6465725369676e61747572650000000468617368")
        val transaferTransaction = mutableListOf(TransferTransactionDto(TransferTransactionData(1, 1, "recipientAddress",
            "senderKey"), 1, "senderAddress", "senderSignature", "hash"))
        val voteTransaction = mutableListOf(VoteTransactionDto(VoteTransactionData(1, 1, "recipientAddress", "senderAddress",
            1, "key"), 1, "senderKey", "senderSignature", "hash"))
        val delegateTransaction = mutableListOf(DelegateTransactionDto(DelegateTransactionData(1, 1, "recipientAddress",
            "senderKey", "delegateKey"), 1, "senderAddress", "senderSignature", "hash"))
        val rewardTransaction = mutableListOf(RewardTransactionDto(RewardTransactionData(1, 1, "recipientAddress",
            "senderKey"), 1, "senderAddress", "senderSignature", "hash"))
        val packet = NetworkMainBlock(1, "previousHash", 1, "merkleHash", "hash", "signature", "key", transaferTransaction,
            voteTransaction, delegateTransaction, rewardTransaction)

        val actualPacket = write(packet, buf)

        assertThat((actualPacket as NetworkMainBlock).hash).isEqualTo(packet.hash)
    }

    @Test
    fun readShouldFillEntityWithExactValuesFromBlockApprovalMessage() {
        val message = BlockApprovalMessage(
            ObserverStage.COMMIT,
            1L,
            "hash",
            "publicKey"
        )

        val result = NetworkBlockApprovalMessage(message)

        assertThat(result.hash).isEqualTo(message.hash)
        assertThat(result.height).isEqualTo(message.height)
        assertThat(result.publicKey).isEqualTo(message.publicKey)
        assertThat(result.stage.value).isEqualTo(message.stage.value)
    }

    @Test
    fun readShouldFillEntityWithExactValuesFromObserverStage() {
        val stage = ObserverStage.COMMIT

        val result = NetworkObserverStage(stage)

        assertThat(result.value).isEqualTo(stage.value)
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