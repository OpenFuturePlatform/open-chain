package io.openfuture.chain.core.model.entity

import io.openfuture.chain.core.model.converter.StateConverter
import io.openfuture.chain.core.model.entity.base.BaseModel
import io.openfuture.chain.core.util.ByteConstants.BYTE
import io.openfuture.chain.core.util.ByteConstants.INT_BYTES
import io.openfuture.chain.core.util.ByteConstants.LONG_BYTES
import org.apache.commons.lang3.StringUtils
import java.nio.ByteBuffer
import javax.persistence.Column
import javax.persistence.Convert
import javax.persistence.Entity
import javax.persistence.Table
import kotlin.text.Charsets.UTF_8

@Entity
@Table(name = "states")
class State(

    @Column(name = "address", nullable = false)
    var address: String,

    @Convert(converter = StateConverter::class)
    @Column(name = "data", nullable = false)
    var data: Data,

    @Column(name = "height_block", nullable = false)
    var heightBlock: Long,

    id: Long = 0L

) : BaseModel(id) {

    fun getBytes(): ByteArray {
        return ByteBuffer.allocate(address.toByteArray(UTF_8).size + data.getBytes().size + LONG_BYTES)
            .put(address.toByteArray(UTF_8))
            .put(data.getBytes())
            .putLong(heightBlock)
            .array()
    }

    data class Data(
        var balance: Long = 0L,
        val votes: MutableList<String> = mutableListOf(),
        var ownVotesCount: Int = 0,
        var isDelegate: Boolean = false
    ) {

        fun getBytes(): ByteArray {
            val voteBytes = votes.joinToString(StringUtils.EMPTY).toByteArray(UTF_8)
            return ByteBuffer.allocate(LONG_BYTES + voteBytes.size + INT_BYTES + BYTE)
                .putLong(balance)
                .put(voteBytes)
                .putInt(ownVotesCount)
                .put((if (isDelegate) 1 else 0).toByte())
                .array()
        }

    }

}

