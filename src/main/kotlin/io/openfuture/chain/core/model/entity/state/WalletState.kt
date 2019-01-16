package io.openfuture.chain.core.model.entity.state

import io.openfuture.chain.core.model.entity.state.payload.StatePayload
import io.openfuture.chain.core.model.entity.state.payload.WalletPayload
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "wallet_states")
class WalletState(
    address: String,
    heightBlock: Long,

    @Embedded
    var payload: WalletPayload

) : State(address, heightBlock) {

    override fun getStatePayload(): StatePayload = payload

}