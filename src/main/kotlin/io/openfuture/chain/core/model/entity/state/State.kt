package io.openfuture.chain.core.model.entity.state

import io.openfuture.chain.core.model.entity.base.BaseModel
import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.network.message.core.StateMessage
import javax.persistence.*

@Entity
@Table(name = "states")
@Inheritance(strategy = InheritanceType.JOINED)
abstract class State(

    @Column(name = "address", nullable = false)
    var address: String,

    @ManyToOne
    @JoinColumn(name = "block_id", nullable = false)
    var block: Block

) : BaseModel() {

    abstract fun toMessage(): StateMessage

}

