package io.openfuture.chain.network.domain

class NetworkVoteTransactionTest {

   /* private val buffer = Unpooled.buffer().writeBytes(ByteBufUtil.decodeHexDump(
        "0000000000000001000000000000000100000010726563697069656e74416464726573730000000973656e6465724b65790000000" +
            "10000000b64656c65676174654b657900000000000000010000000f73656e6465725075626c69634b65790000000f73656e6465725369676e61747572650000000468617368"))
    private val entity = VoteTransactionDto(VoteTransactionData(1, 1, "recipientAddress", "senderKey", 1, "delegateKey"),
        1, "senderPublicKey", "senderSignature", "hash")


    @Test
    fun writeShouldWriteExactValuesInBuffer() {
        val actualBuffer = Unpooled.buffer()

        entity.write(actualBuffer)

        assertThat(actualBuffer).isEqualTo(buffer)
    }

    @Test
    fun readShouldFillEntityWithExactValuesFromBuffer() {
        val actualEntity = VoteTransactionDto::class.java.newInstance()

        actualEntity.read(buffer)

        assertThat(actualEntity.hash).isEqualTo(entity.hash)
    }*/

}