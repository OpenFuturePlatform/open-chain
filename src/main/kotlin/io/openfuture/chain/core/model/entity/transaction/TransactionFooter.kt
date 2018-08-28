package io.openfuture.chain.core.model.entity.transaction

import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class TransactionFooter (

    @Column(name = "hash", nullable = false, unique = true)
    var hash: String,

    @Column(name = "sender_signature", nullable = false)
    var senderSignature: String,

    @Column(name = "sender_key", nullable = false)
    var senderPublicKey: String

)