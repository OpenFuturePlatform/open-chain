package io.openfuture.chain.core.model.entity.state

import io.openfuture.chain.core.model.entity.block.Block
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.network.message.core.AccountStateMessage
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "account_states")
class AccountState(
    address: String,
    block: Block,

    @Column(name = "balance", nullable = false)
    val balance: Long = 0,

    @Column(name = "vote_for")
    val voteFor: String? = null,

    @Column(name = "storage")
    val storage: String? = null

) : State(address, block) {

    companion object {
        fun of(message: AccountStateMessage, block: MainBlock): AccountState =
            AccountState(message.address, block, message.balance, message.voteFor, message.storage)
    }


    override fun toMessage(): AccountStateMessage = AccountStateMessage(address, balance, voteFor, storage)

}