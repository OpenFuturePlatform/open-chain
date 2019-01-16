package io.openfuture.chain.core.model.entity.state

import io.openfuture.chain.core.model.entity.state.payload.NodePayload
import io.openfuture.chain.core.model.entity.state.payload.StatePayload
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "node_states")
class NodeState(
    address: String,
    heightBlock: Long,

    @Embedded
    var payload: NodePayload

) : State(address, heightBlock) {

    override fun getStatePayload(): StatePayload = payload

}