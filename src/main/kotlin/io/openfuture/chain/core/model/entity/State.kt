package io.openfuture.chain.core.model.entity

import io.openfuture.chain.core.model.converter.StateConverter
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
    var data: Data,

    @ManyToOne
    @JoinColumn(name = "block_id")
    var block: Block,

    id: Long = 0L

) : BaseModel(id) {

    data class Data(
        var balance: Long = 0L,
        var votes: MutableList<String> = mutableListOf(),
        var isDelegate: Boolean = false
    )

}

