package io.openfuture.chain.network.message.application.transaction

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class DelegateTransactionMessageTest {

    private lateinit var message: DelegateTransactionMessage
    private lateinit var buffer: ByteBuf


    @Before
    fun setup(){
        buffer = createBuffer("000000000001e0f300000000000000010000000d73656e646572416464726573730000000f73656e646572507" +
            "5626c69634b65790000000f73656e6465725369676e617475726500000004686173680000000b64656c65676174654b6579")
        message = DelegateTransactionMessage(123123, 1, "senderAddress", "senderPublicKey", "senderSignature", "hash",
            "delegateKey")
    }

    @Test
    fun writeShouldWriteExactValuesInBuffer() {
        val actualBuffer = Unpooled.buffer()

        message.write(actualBuffer)

        assertThat(actualBuffer).isEqualTo(buffer)
    }

    @Test
    fun readShouldFillEntityWithExactValuesFromBuffer() {
        val actualMessage = DelegateTransactionMessage::class.java.newInstance()

        actualMessage.read(buffer)

        assertThat(actualMessage).isEqualToComparingFieldByFieldRecursively(message)
    }

    private fun createBuffer(value: String) : ByteBuf = Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump((value)))

}