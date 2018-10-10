package io.openfuture.chain.core.model.entity.block.payload

import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.util.ByteConstants.LONG_BYTES
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets.UTF_8
import javax.persistence.*

@Embeddable
class GenesisBlockPayload(

    @Column(name = "epoch_index", nullable = false)
    var epochIndex: Long,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "delegate2genesis",
        joinColumns = [(JoinColumn(name = "genesis_id"))],
        inverseJoinColumns = [(JoinColumn(name = "delegate_id"))])
    var activeDelegates: List<Delegate>

) : BlockPayload {

    override fun getBytes(): ByteArray {
        val keys = activeDelegates.map { it.publicKey }
        val keysLength = keys.asSequence().map { it.toByteArray(UTF_8).size }.sum()

        val buffer = ByteBuffer.allocate(LONG_BYTES + keysLength)
        buffer.putLong(epochIndex)
        keys.forEach { buffer.put(it.toByteArray(UTF_8)) }
        return buffer.array()
    }

}