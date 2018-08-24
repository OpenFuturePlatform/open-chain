package io.openfuture.chain.network.message.network

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.openfuture.chain.config.MessageTests
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class ExplorerAddressesMessageTest : MessageTests() {

    private lateinit var message: ExplorerAddressesMessage
    private lateinit var buffer: ByteBuf


    @Before
    fun setup() {
        buffer = createBuffer("0000000200000003756964000000093132372e302e302e310000238200000003756964000000093" +
            "132372e302e302e3100002383")
        message = ExplorerAddressesMessage(listOf(
            AddressMessage("uid", NetworkAddressMessage("127.0.0.1", 9090)),
            AddressMessage("uid", NetworkAddressMessage("127.0.0.1", 9091))
        ))
    }

    @Test
    fun writeShouldWriteExactValuesInBuffer() {
        val actualBuffer = Unpooled.buffer()

        message.write(actualBuffer)

        assertThat(actualBuffer).isEqualTo(buffer)
    }

    @Test
    fun readShouldFillEntityWithExactValuesFromBuffer() {
        val actualMessage = ExplorerAddressesMessage::class.java.newInstance()

        actualMessage.read(buffer)

        assertThat(actualMessage).isEqualToComparingFieldByFieldRecursively(message)
    }

}