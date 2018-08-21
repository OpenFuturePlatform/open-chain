package io.openfuture.chain.core.model.entity.transaction

import io.openfuture.chain.core.model.entity.base.BaseModel
import javax.persistence.Column
import javax.persistence.Embedded
import javax.persistence.MappedSuperclass

@MappedSuperclass
abstract class BaseTransaction(

    @Embedded
    val header: TransactionHeader,

    @Column(name = "hash", nullable = false, unique = true)
    var hash: String,

    @Column(name = "sender_signature", nullable = false)
    var senderSignature: String,

    @Column(name = "sender_key", nullable = false)
    var senderPublicKey: String

) : BaseModel()