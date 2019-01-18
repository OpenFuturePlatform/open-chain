package io.openfuture.chain.core.model.entity.block.payload

import io.openfuture.chain.core.util.ByteConstants.LONG_BYTES
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8
import javax.persistence.CollectionTable
import javax.persistence.Column
import javax.persistence.ElementCollection
import javax.persistence.Embeddable

@Embeddable
class GenesisBlockPayload(

    @Column(name = "epoch_index", nullable = false)
    var epochIndex: Long,

    @ElementCollection
    @CollectionTable(name = "delegate2genesis")
    var activeDelegates: MutableList<String>

) : BlockPayload {

    override fun getBytes(): ByteArray {
        val length = activeDelegates.asSequence().map { it.toByteArray(UTF_8).size }.sum()
        val buffer = ByteBuffer.allocate(LONG_BYTES + length)
        buffer.putLong(epochIndex)
        activeDelegates.forEach { buffer.put(it.toByteArray(UTF_8)) }
        return buffer.array()
    }

}