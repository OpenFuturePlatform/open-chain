package io.openfuture.chain.entity.transaction.base

import io.openfuture.chain.entity.base.BaseModel
import javax.persistence.Column
import javax.persistence.MappedSuperclass

@MappedSuperclass
abstract class BaseTransaction(

    @Column(name = "timestamp", nullable = false)
    var timestamp: Long,

    @Column(name = "amount", nullable = false)
    var amount: Long,

    @Column(name = "fee", nullable = false)
    var fee: Long,

    @Column(name = "recipient_address", nullable = false)
    var recipientAddress: String,

    @Column(name = "sender_address", nullable = false)
    var senderAddress: String,

    @Column(name = "sender_key", nullable = false)
    var senderPublicKey: String,

    @Column(name = "sender_signature", nullable = false)
    var senderSignature: String,

    @Column(name = "hash", nullable = false, unique = true)
    var hash: String

) : BaseModel()