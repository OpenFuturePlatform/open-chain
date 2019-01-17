package io.openfuture.chain.core.model.entity.state.payload

import io.openfuture.chain.core.util.ByteConstants.LONG_BYTES
import java.nio.ByteBuffer
import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
data class DelegatePayload(

    @Column(name = "rating", nullable = false)
    val rating: Long = 0

) : StatePayload {

    override fun getBytes(): ByteArray = ByteBuffer.allocate(LONG_BYTES).putLong(rating).array()

}