package io.openfuture.chain.core.model.entity.state.payload

import io.openfuture.chain.core.model.converter.NodePayloadConverter
import io.openfuture.chain.core.util.ByteConstants.LONG_BYTES
import org.apache.commons.lang3.StringUtils.EMPTY
import java.nio.ByteBuffer
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Embeddable
import kotlin.text.Charsets.UTF_8

@Embeddable
data class NodePayload(

    @Convert(converter = NodePayloadConverter::class)
    @Column(name = "data", nullable = false)
    val data: Data

) : StatePayload {

    override fun getBytes(): ByteArray = data.getBytes()

    data class Data(
        var address: String,
        val registrationDate: Long,
        val ownVotes: MutableList<String> = mutableListOf() // list of addresses
    ) {

        fun getBytes(): ByteArray {
            val voteBytes = ownVotes.joinToString(EMPTY).toByteArray(UTF_8)
            return ByteBuffer.allocate(address.toByteArray(UTF_8).size + LONG_BYTES + voteBytes.size)
                .put(address.toByteArray(UTF_8))
                .putLong(registrationDate)
                .put(voteBytes)
                .array()
        }

    }

}