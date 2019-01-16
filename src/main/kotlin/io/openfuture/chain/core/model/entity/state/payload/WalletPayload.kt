package io.openfuture.chain.core.model.entity.state.payload

import io.openfuture.chain.core.model.converter.WalletPayloadConverter
import io.openfuture.chain.core.util.ByteConstants.LONG_BYTES
import org.apache.commons.lang3.StringUtils.EMPTY
import java.nio.ByteBuffer
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Embeddable
import kotlin.text.Charsets.UTF_8

@Embeddable
data class WalletPayload(

    @Convert(converter = WalletPayloadConverter::class)
    @Column(name = "data", nullable = false)
    val data: Data

) : StatePayload {

    override fun getBytes(): ByteArray = data.getBytes()

    data class Data(
        var balance: Long = 0L,
        val votes: MutableList<String> = mutableListOf() // list of nodeIds
    ) {

        fun getBytes(): ByteArray {
            val voteBytes = votes.joinToString(EMPTY).toByteArray(UTF_8)
            return ByteBuffer.allocate(LONG_BYTES + voteBytes.size)
                .putLong(balance)
                .put(voteBytes)
                .array()
        }

    }

}