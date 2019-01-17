package io.openfuture.chain.core.model.entity.state

import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.network.message.core.WalletStateMessage
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "wallet_states")
class WalletState(
    address: String,
    block: MainBlock,

    @Column(name = "balance", nullable = false)
    val balance: Long = 0

) : State(address, block) {

    companion object {
        fun of(message: WalletStateMessage, block: MainBlock): WalletState =
            WalletState(message.address, block, message.balance)
    }


    override fun toMessage(): WalletStateMessage = WalletStateMessage(address, balance)

}