package io.openfuture.chain.network.message.base

import io.netty.buffer.ByteBuf
import io.openfuture.chain.network.message.base.PacketType.*
import io.openfuture.chain.network.message.consensus.BlockApprovalMessage
import io.openfuture.chain.network.message.consensus.PendingBlockMessage
import io.openfuture.chain.network.message.core.*
import io.openfuture.chain.network.message.network.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.util.*

class PacketTypeTests {

    @Test
    fun getShouldReturnTypeForEachKnownId() {
        assertThat(PacketType.get(1)).isEqualTo(ADDRESSES)
        assertThat(PacketType.get(2)).isEqualTo(FIND_ADDRESSES)
        assertThat(PacketType.get(3)).isEqualTo(GREETING)
        assertThat(PacketType.get(4)).isEqualTo(HEART_BEAT)
        assertThat(PacketType.get(5)).isEqualTo(ASK_TIME)
        assertThat(PacketType.get(6)).isEqualTo(TIME)
        assertThat(PacketType.get(7)).isEqualTo(SYNC_BLOCKS_REQUEST)
        assertThat(PacketType.get(8)).isEqualTo(MAIN_BLOCK)
        assertThat(PacketType.get(9)).isEqualTo(GENESIS_BLOCK)
        assertThat(PacketType.get(10)).isEqualTo(BLOCK_APPROVAL)
        assertThat(PacketType.get(11)).isEqualTo(PENDING_BLOCK)
        assertThat(PacketType.get(12)).isEqualTo(TRANSFER_TRANSACTION)
        assertThat(PacketType.get(13)).isEqualTo(DELEGATE_TRANSACTION)
        assertThat(PacketType.get(14)).isEqualTo(VOTE_TRANSACTION)
    }

    @Test(expected = NoSuchElementException::class)
    fun getShouldThrowExceptionForUnknownId() {
        assertThat(PacketType.get(15)).isEqualTo(VOTE_TRANSACTION)
    }

    @Test
    fun getShouldReturnTypeForEachKnownInstance() {
        assertThat(PacketType.get(AddressesMessage::class.java.newInstance())).isEqualTo(ADDRESSES)
        assertThat(PacketType.get(FindAddressesMessage::class.java.newInstance())).isEqualTo(FIND_ADDRESSES)
        assertThat(PacketType.get(GreetingMessage::class.java.newInstance())).isEqualTo(GREETING)
        assertThat(PacketType.get(HeartBeatMessage::class.java.newInstance())).isEqualTo(HEART_BEAT)
        assertThat(PacketType.get(AskTimeMessage::class.java.newInstance())).isEqualTo(ASK_TIME)
        assertThat(PacketType.get(TimeMessage::class.java.newInstance())).isEqualTo(TIME)
        assertThat(PacketType.get(SyncBlockRequestMessage::class.java.newInstance())).isEqualTo(SYNC_BLOCKS_REQUEST)
        assertThat(PacketType.get(MainBlockMessage::class.java.newInstance())).isEqualTo(MAIN_BLOCK)
        assertThat(PacketType.get(GenesisBlockMessage::class.java.newInstance())).isEqualTo(GENESIS_BLOCK)
        assertThat(PacketType.get(BlockApprovalMessage::class.java.newInstance())).isEqualTo(BLOCK_APPROVAL)
        assertThat(PacketType.get(PendingBlockMessage::class.java.newInstance())).isEqualTo(PENDING_BLOCK)
        assertThat(PacketType.get(TransferTransactionMessage::class.java.newInstance())).isEqualTo(TRANSFER_TRANSACTION)
        assertThat(PacketType.get(DelegateTransactionMessage::class.java.newInstance())).isEqualTo(DELEGATE_TRANSACTION)
        assertThat(PacketType.get(VoteTransactionMessage::class.java.newInstance())).isEqualTo(VOTE_TRANSACTION)
    }

    @Test(expected = NoSuchElementException::class)
    fun getShouldThrowExceptionForUnknownInstance() {
        val instance = object : BaseMessage {

            override fun read(buffer: ByteBuf) {}

            override fun write(buffer: ByteBuf) {}

        }

        PacketType.get(instance)
    }

}