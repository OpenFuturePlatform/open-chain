package io.openfuture.chain.core.model.entity.transaction.unconfirmed

import io.openfuture.chain.core.model.entity.transaction.BaseTransaction
import io.openfuture.chain.network.message.core.TransactionMessage
import javax.persistence.Entity
import javax.persistence.Inheritance
import javax.persistence.InheritanceType
import javax.persistence.Table

@Entity
@Table(name = "u_transactions")
@Inheritance(strategy = InheritanceType.JOINED)
abstract class UnconfirmedTransaction(
    timestamp: Long,
    fee: Long,
    senderAddress: String,
    hash: String,
    senderSignature: String,
    senderPublicKey: String
) : BaseTransaction(timestamp, fee, senderAddress, hash, senderSignature, senderPublicKey) {

    abstract fun toMessage() : TransactionMessage

}