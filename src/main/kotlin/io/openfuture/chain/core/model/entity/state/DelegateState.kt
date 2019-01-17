package io.openfuture.chain.core.model.entity.state

import io.openfuture.chain.core.model.entity.state.payload.DelegatePayload
import io.openfuture.chain.core.model.entity.state.payload.StatePayload
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "delegate_states")
class DelegateState(
    address: String,
    heightBlock: Long,

    @Embedded
    var payload: DelegatePayload

) : State(address, heightBlock) {

    override fun getStatePayload(): StatePayload = payload

}