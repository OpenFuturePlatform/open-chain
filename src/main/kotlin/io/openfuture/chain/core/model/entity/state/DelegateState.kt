package io.openfuture.chain.core.model.entity.state

import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.network.message.core.DelegateStateMessage
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "delegate_states")
class DelegateState(
    address: String,
    block: MainBlock,

    @Column(name = "rating", nullable = false)
    val rating: Long = 0

) : State(address, block) {

    companion object {
        fun of(message: DelegateStateMessage, block: MainBlock): DelegateState =
            DelegateState(message.address, block, message.rating)
    }


    override fun toMessage(): DelegateStateMessage = DelegateStateMessage(address, rating)

}