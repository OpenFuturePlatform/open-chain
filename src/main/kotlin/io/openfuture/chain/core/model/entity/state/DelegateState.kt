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
    val rating: Long,

    @Column(name = "wallet_address", nullable = false)
    var walletAddress: String,

    @Column(name = "create_date", nullable = false)
    var createDate: Long

) : State(address, block) {

    companion object {
        fun of(message: DelegateStateMessage, block: MainBlock): DelegateState =
            DelegateState(message.address, block, message.rating, message.walletAddress, message.createDate)
    }


    override fun toMessage(): DelegateStateMessage = DelegateStateMessage(address, rating, walletAddress, createDate)

}