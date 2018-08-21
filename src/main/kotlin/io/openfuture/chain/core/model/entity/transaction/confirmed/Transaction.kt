package io.openfuture.chain.core.model.entity.transaction.confirmed

import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.BaseTransaction
import io.openfuture.chain.core.model.entity.transaction.TransactionHeader
import io.openfuture.chain.network.message.core.TransactionMessage
import javax.persistence.*

@Entity
@Table(name = "transactions")
@Inheritance(strategy = InheritanceType.JOINED)
abstract class Transaction(
    header: TransactionHeader,
    hash: String,
    senderSignature: String,
    senderPublicKey: String,

    @ManyToOne
    @JoinColumn(name = "block_id", nullable = false)
    var block: MainBlock

) : BaseTransaction(header, hash, senderSignature, senderPublicKey) {

    abstract fun toMessage(): TransactionMessage

}