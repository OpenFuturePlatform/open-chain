package io.openfuture.chain.core.model.entity

import io.openfuture.chain.core.converter.StateConverter
import io.openfuture.chain.core.model.entity.base.BaseModel
import io.openfuture.chain.core.model.entity.block.Block
import javax.persistence.*

@Entity
@Table(name = "states")
class State(

    @Column(name = "address", nullable = false)
    var address: String,

    @Convert(converter = StateConverter::class)
    @Column(name = "data", nullable = false)
    var data: WalletSnapshot,

    @ManyToOne
    @JoinColumn(name = "block_id")
    var block: Block,

    id: Long = 0L

) : BaseModel(id) {

    data class WalletSnapshot(
        var balance: Long = 0L,
        var votes: List<String> = emptyList(),
        var isDelegate: Boolean = false
    )

    override fun toString(): String {
        return "State(address='$address', data=$data, block=$block)"
    }


}

