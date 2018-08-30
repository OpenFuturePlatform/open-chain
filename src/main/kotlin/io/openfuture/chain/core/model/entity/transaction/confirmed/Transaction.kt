package io.openfuture.chain.core.model.entity.transaction.confirmed

import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.BaseTransaction
import io.openfuture.chain.core.model.entity.transaction.TransactionFooter
import io.openfuture.chain.core.model.entity.transaction.TransactionHeader
import io.openfuture.chain.network.message.core.TransactionMessage
import javax.persistence.*

@Entity
@Table(name = "transactions")
@Inheritance(strategy = InheritanceType.JOINED)
abstract class Transaction(
    header: TransactionHeader,
    footer: TransactionFooter,

    @ManyToOne
    @JoinColumn(name = "block_id", nullable = false)
    var block: MainBlock

) : BaseTransaction(header, footer) {

    abstract fun toMessage(): TransactionMessage

}