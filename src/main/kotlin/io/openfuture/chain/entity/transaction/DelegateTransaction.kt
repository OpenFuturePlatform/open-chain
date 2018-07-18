package io.openfuture.chain.entity.transaction

import io.openfuture.chain.entity.MainBlock
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "delegate_transactions")
class DelegateTransaction(timestamp: Long, amount: Double, recipientAddress: String, senderKey: String,
                          senderAddress: String, senderSignature: String, hash: String,

    @Column(name = "key", nullable = false)
    var key: String,

    @Column(name = "host", nullable = false)
    var host: String,

    @Column(name = "port", nullable = false)
    var port: Int,

    block: MainBlock? = null

) : BaseTransaction(timestamp, amount, recipientAddress, senderKey, senderAddress,
    senderSignature, hash, block)