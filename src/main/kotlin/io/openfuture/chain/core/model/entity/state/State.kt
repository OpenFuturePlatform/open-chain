package io.openfuture.chain.core.model.entity.state

import io.openfuture.chain.core.model.entity.base.BaseModel
import io.openfuture.chain.core.model.entity.state.payload.StatePayload
import io.openfuture.chain.core.util.ByteConstants.LONG_BYTES
import java.nio.ByteBuffer
import javax.persistence.*
import kotlin.text.Charsets.UTF_8

@Entity
@Table(name = "states")
@Inheritance(strategy = InheritanceType.JOINED)
abstract class State(

    @Column(name = "address", nullable = false)
    var address: String,

    @Column(name = "height_block", nullable = false)
    var heightBlock: Long

) : BaseModel() {

    abstract fun getStatePayload(): StatePayload

    fun getBytes(): ByteArray {
        return ByteBuffer.allocate(address.toByteArray(UTF_8).size + getStatePayload().getBytes().size + LONG_BYTES)
            .put(address.toByteArray(UTF_8))
            .put(getStatePayload().getBytes())
            .putLong(heightBlock)
            .array()
    }

}

