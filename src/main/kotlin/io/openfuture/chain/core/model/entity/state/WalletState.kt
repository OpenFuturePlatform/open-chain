package io.openfuture.chain.core.model.entity.state

import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.network.message.core.WalletStateMessage
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "wallet_states")
class WalletState(
    address: String,
    block: Block,

    @Column(name = "balance", nullable = false)
    val balance: Long = 0,

    @Column(name = "vote_for")
    val voteFor: String? = null

) : State(address, block) {

    companion object {
        fun of(message: WalletStateMessage, block: MainBlock): WalletState =
            WalletState(message.address, block, message.balance, message.voteFor)
    }


    override fun toMessage(): WalletStateMessage = WalletStateMessage(address, balance, voteFor)

}