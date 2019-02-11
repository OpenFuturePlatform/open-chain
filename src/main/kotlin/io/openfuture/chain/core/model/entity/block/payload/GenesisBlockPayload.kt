package io.openfuture.chain.core.model.entity.block.payload

import java.nio.ByteBuffer
import javax.persistence.*
import kotlin.Long.Companion.SIZE_BYTES

@Embeddable
class GenesisBlockPayload(

    @Column(name = "epoch_index", nullable = false)
    var epochIndex: Long,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "delegate2genesis", joinColumns = [JoinColumn(name = "genesis_id")])
    @Column(name = "public_key")
    var activeDelegates: List<String>

) : BlockPayload {

    override fun getBytes(): ByteArray {
        val length = activeDelegates.asSequence().map { it.toByteArray().size }.sum()
        val buffer = ByteBuffer.allocate(SIZE_BYTES + length)
        buffer.putLong(epochIndex)
        activeDelegates.forEach { buffer.put(it.toByteArray()) }
        return buffer.array()
    }

}