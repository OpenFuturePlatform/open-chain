package io.openfuture.chain.core.model.entity.transaction

import javax.persistence.Column
import javax.persistence.Embeddable

@Embeddable
class TransactionHeader(

    @Column(name = "timestamp", nullable = false)
    var timestamp: Long,

    @Column(name = "fee", nullable = false)
    var fee: Long,

    @Column(name = "sender_address", nullable = false)
    var senderAddress: String

)