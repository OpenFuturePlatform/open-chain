package io.openfuture.chain.core.model.entity.state

import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.network.message.core.ContractStateMessage
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "contract_states")
class ContractState(
    address: String,
    block: MainBlock,

    @Column(name = "storage", nullable = false)
    val storage: String

) : State(address, block) {

    companion object {
        fun of(message: ContractStateMessage, block: MainBlock): ContractState =
            ContractState(message.address, block, message.storage)
    }

    override fun toMessage(): ContractStateMessage = ContractStateMessage(address, storage)

}