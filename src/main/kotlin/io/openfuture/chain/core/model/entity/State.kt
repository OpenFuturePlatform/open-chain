package io.openfuture.chain.core.model.entity

import io.openfuture.chain.core.model.converter.StateConverter
import io.openfuture.chain.core.model.entity.base.BaseModel
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.util.ByteConstants.BYTE
import io.openfuture.chain.core.util.ByteConstants.LONG_BYTES
import java.nio.ByteBuffer
import javax.persistence.*
import kotlin.text.Charsets.UTF_8

@Entity
@Table(name = "states")
class State(

    @Column(name = "address", nullable = false)
    var address: String,

    @Convert(converter = StateConverter::class)
    @Column(name = "data", nullable = false)
    var data: Data,

    @ManyToOne
    @JoinColumn(name = "block_id")
    var block: Block,

    id: Long = 0L

) : BaseModel(id) {

    fun getBytes(): ByteArray {
        return ByteBuffer.allocate(address.toByteArray(UTF_8).size + data.getBytes().size + LONG_BYTES)
            .put(address.toByteArray(UTF_8))
            .put(data.getBytes())
            .putLong(block.id)
            .array()
    }

    data class Data(
        var balance: Long = 0L,
        val votes: MutableList<String> = mutableListOf(),
        var isDelegate: Boolean = false
    ) {

        fun getBytes(): ByteArray {
            val voteBytes = votes.joinToString(separator = "").toByteArray(UTF_8)
            return ByteBuffer.allocate(LONG_BYTES + voteBytes.size + BYTE)
                .putLong(balance)
                .put(voteBytes)
                .put((if (isDelegate) 1 else 0).toByte())
                .array()
        }

    }

}

