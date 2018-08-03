package io.openfuture.chain.network.message.network.address

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.openfuture.chain.config.MessageTests
import io.openfuture.chain.network.message.network.AddressesMessage
import io.openfuture.chain.network.message.network.NetworkAddressMessage
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Test

class AddressesMessageTests : MessageTests() {

    private lateinit var message: AddressesMessage
    private lateinit var buffer: ByteBuf


    @Before
    fun setup() {
        buffer = createBuffer("00000002000000093132372e302e302e3100002382000000093132372e302e302e3100002383")
        message = AddressesMessage(listOf(
            NetworkAddressMessage("127.0.0.1", 9090),
            NetworkAddressMessage("127.0.0.1", 9091)))
    }

    @Test
    fun writeShouldWriteExactValuesInBuffer() {
        val actualBuffer = Unpooled.buffer()

        message.write(actualBuffer)

        assertThat(actualBuffer).isEqualTo(buffer)
    }

    @Test
    fun readShouldFillEntityWithExactValuesFromBuffer() {
        val actualMessage = AddressesMessage::class.java.newInstance()

        actualMessage.read(buffer)

        assertThat(actualMessage).isEqualToComparingFieldByFieldRecursively(message)
    }

}