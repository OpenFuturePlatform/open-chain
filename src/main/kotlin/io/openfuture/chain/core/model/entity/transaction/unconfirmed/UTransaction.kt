package io.openfuture.chain.core.model.entity.transaction.unconfirmed

import io.openfuture.chain.core.model.entity.transaction.BaseTransaction
import io.openfuture.chain.core.model.entity.transaction.payload.BaseTransactionPayload
import javax.persistence.Entity
import javax.persistence.Inheritance
import javax.persistence.InheritanceType
import javax.persistence.Table

@Entity
@Table(name = "u_transactions")
@Inheritance(strategy = InheritanceType.JOINED)
abstract class UTransaction<T : BaseTransactionPayload>(
    timestamp: Long,
    payload: T,
    senderAddress: String,
    senderPublicKey: String,
    senderSignature: String,
    hash: String
) : BaseTransaction<T>(timestamp, payload, senderAddress, senderPublicKey, senderSignature, hash) {

//    abstract fun toMessage() : BaseTransactionDto

//    abstract fun toConfirmed(): Transaction

}