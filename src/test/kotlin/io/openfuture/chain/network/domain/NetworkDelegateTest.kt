package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled
import io.openfuture.chain.network.domain.application.delegate.DelegateMessage

class NetworkDelegateTest {

    private val buffer = Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump(
        "000000093132372e302e302e31000000036b6579"))
    private val entity = DelegateMessage("127.0.0.1", "key")

/*

    @Test
    fun writeShouldWriteExactValuesInBuffer() {
        val actualBuffer = Unpooled.buffer()

        entity.write(actualBuffer)

        assertThat(actualBuffer).isEqualTo(buffer)
    }

    @Test
    fun readShouldFillEntityWithExactValuesFromBuffer() {
        val actualEntity = DelegateMessage::class.java.newInstance()

        actualEntity.read(buffer)

        assertThat(actualEntity).isEqualTo(entity)
    }
*/

}