package io.openfuture.chain.network.domain

import io.netty.buffer.ByteBufUtil
import io.netty.buffer.Unpooled
import io.openfuture.chain.network.domain.network.address.NetworkAddressMessage

class NetworkAddressTest {

    private val buffer = Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump(
        "000000093132372e302e302e3100002382"))
    private val entity = NetworkAddressMessage("127.0.0.1", 9090)
/*


    @Test
    fun writeShouldWriteExactValuesInBuffer() {
        val actualBuffer = Unpooled.buffer()

        entity.write(actualBuffer)

        assertThat(actualBuffer).isEqualTo(buffer)
    }

    @Test
    fun readShouldFillEntityWithExactValuesFromBuffer() {
        val actualEntity = NetworkAddressMessage::class.java.newInstance()

        actualEntity.read(buffer)

        assertThat(actualEntity).isEqualTo(entity)
    }
*/

}