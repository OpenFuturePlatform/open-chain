package io.openfuture.chain.network.message.core

import io.netty.buffer.ByteBuf
import io.openfuture.chain.config.MessageTests
import org.junit.Before

class GenesisBlockMessageTests : MessageTests() {

    private lateinit var message: GenesisBlockMessage
    private lateinit var buffer: ByteBuf


    @Before
    fun setup() {
        buffer = createBuffer("000000010000000c70726576696f7573486173680000000000000001000000000000000a00000" +
            "0097075626c69634b65790000000468617368000000097369676e61747572650000000000000001")

        message = GenesisBlockMessage(1, "previousHash", 1, 10, "hash", "signature", "publicKey", 1, listOf("publicKey"))
    }

//    @Test
//    fun writeShouldWriteExactValuesInBuffer() {
//        val actualBuffer = Unpooled.buffer()
//
//        message.write(actualBuffer)
//
//        assertThat(actualBuffer).isEqualTo(buffer)
//    }
//
//    @Test
//    fun readShouldFillEntityWithExactValuesFromBuffer() {
//        val actualMessage = GenesisBlockMessage::class.java.newInstance()
//
//        actualMessage.read(buffer)
//
//        assertThat(actualMessage).isEqualToComparingFieldByFieldRecursively(message)
//    }

}