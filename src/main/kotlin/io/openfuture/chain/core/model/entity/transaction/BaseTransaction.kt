package io.openfuture.chain.core.model.entity.transaction

import io.openfuture.chain.core.model.entity.base.BaseModel
import io.openfuture.chain.core.model.entity.transaction.payload.BaseTransactionPayload
import javax.persistence.Column
import javax.persistence.Embedded
import javax.persistence.MappedSuperclass

@MappedSuperclass
abstract class BaseTransaction(

    @Column(name = "timestamp", nullable = false)
    var timestamp: Long,

    @Column(name = "sender_address", nullable = false)
    var senderAddress: String,

    @Column(name = "sender_key", nullable = false)
    var senderPublicKey: String,

    @Column(name = "sender_signature", nullable = false)
    var senderSignature: String,

    @Column(name = "hash", nullable = false, unique = true)
    var hash: String

) : BaseModel() {

    abstract fun getPayload(): BaseTransactionPayload

}