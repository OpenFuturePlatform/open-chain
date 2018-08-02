package io.openfuture.chain.core.model.entity.transaction.unconfirmed

import io.openfuture.chain.core.model.dto.transaction.BaseTransactionDto
import io.openfuture.chain.core.model.entity.transaction.BaseTransaction
import io.openfuture.chain.core.model.entity.transaction.confirmed.Transaction
import javax.persistence.Entity
import javax.persistence.Inheritance
import javax.persistence.InheritanceType
import javax.persistence.Table

@Entity
@Table(name = "u_transactions")
@Inheritance(strategy = InheritanceType.JOINED)
abstract class UTransaction(
    timestamp: Long,
    senderAddress: String,
    senderPublicKey: String,
    senderSignature: String,
    hash: String
) : BaseTransaction(timestamp, senderAddress, senderPublicKey, senderSignature, hash) {

    abstract fun toMessage() : BaseTransactionDto

    abstract fun toConfirmed() : Transaction

}