package io.openfuture.chain.core.model.entity.state

import io.openfuture.chain.core.model.entity.base.BaseModel
import io.openfuture.chain.network.message.core.StateMessage
import javax.persistence.*

@Entity
@Table(name = "states")
@Inheritance(strategy = InheritanceType.JOINED)
abstract class State(

    @Column(name = "address", nullable = false)
    var address: String,

    @Column(name = "hash", nullable = false)
    var hash: String

) : BaseModel() {

    abstract fun getBytes(): ByteArray

    abstract fun toMessage(): StateMessage

}