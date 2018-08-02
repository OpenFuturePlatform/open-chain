package io.openfuture.chain.network.message.application.block

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class BlockApprovalMessageTest {

    private lateinit var message: BlockApprovalMessage
    private lateinit var buffer: ByteBuf

    @Before
    fun setup(){
        buffer = createBuffer("000100000000000000010000000468617368000000097075626c69634b6579000000097369676e6174757265")
        message = BlockApprovalMessage(1, 1, "hash", "publicKey", "signature")
    }

    @Test
    fun writeShouldWriteExactValuesInBuffer() {
        val actualBuffer = Unpooled.buffer()

        message.write(actualBuffer)

        assertThat(actualBuffer).isEqualTo(buffer)
    }

    @Test
    fun readShouldFillEntityWithExactValuesFromBuffer() {
        val actualBlock = BlockApprovalMessage::class.java.newInstance()

        actualBlock.read(buffer)

        assertThat(actualBlock).isEqualToComparingFieldByFieldRecursively(message)
    }

    private fun createBuffer(value: String) : ByteBuf = Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump((value)))

}