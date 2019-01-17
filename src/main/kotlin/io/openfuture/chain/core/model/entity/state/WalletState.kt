package io.openfuture.chain.core.model.entity.state

import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.state.payload.StatePayload
import io.openfuture.chain.core.model.entity.state.payload.WalletPayload
import io.openfuture.chain.network.message.core.WalletStateMessage
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "wallet_states")
class WalletState(
    address: String,
    block: MainBlock,

    @Embedded
    var payload: WalletPayload

) : State(address, block) {

    companion object {
        fun of(message: WalletStateMessage, block: MainBlock): WalletState =
            WalletState(message.address, block, WalletPayload(message.balance))
    }


    override fun getStatePayload(): StatePayload = payload

    override fun toMessage(): WalletStateMessage = WalletStateMessage(address, payload.balance)

}