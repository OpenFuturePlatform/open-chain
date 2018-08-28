package io.openfuture.chain.core.model.entity.transaction.unconfirmed

import io.openfuture.chain.core.model.entity.transaction.BaseTransaction
import io.openfuture.chain.core.model.entity.transaction.TransactionFooter
import io.openfuture.chain.core.model.entity.transaction.TransactionHeader
import io.openfuture.chain.network.message.core.TransactionMessage
import javax.persistence.Entity
import javax.persistence.Inheritance
import javax.persistence.InheritanceType
import javax.persistence.Table

@Entity
@Table(name = "u_transactions")
@Inheritance(strategy = InheritanceType.JOINED)
abstract class UnconfirmedTransaction(
    header: TransactionHeader,
    footer: TransactionFooter
) : BaseTransaction(header, footer) {

    abstract fun toMessage() : TransactionMessage

}