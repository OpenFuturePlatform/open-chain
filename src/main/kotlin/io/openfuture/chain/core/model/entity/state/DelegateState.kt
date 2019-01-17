package io.openfuture.chain.core.model.entity.state

import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.state.payload.DelegatePayload
import io.openfuture.chain.core.model.entity.state.payload.StatePayload
import io.openfuture.chain.network.message.core.DelegateStateMessage
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "delegate_states")
class DelegateState(
    address: String,
    block: MainBlock,

    @Embedded
    var payload: DelegatePayload

) : State(address, block) {

    companion object {
        fun of(message: DelegateStateMessage, block: MainBlock): DelegateState =
            DelegateState(message.address, block, DelegatePayload(message.rating))
    }


    override fun getStatePayload(): StatePayload = payload

    override fun toMessage(): DelegateStateMessage = DelegateStateMessage(address, payload.rating)

}