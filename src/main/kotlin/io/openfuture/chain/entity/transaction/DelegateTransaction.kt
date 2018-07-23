package io.openfuture.chain.entity.transaction

import io.openfuture.chain.entity.MainBlock
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "delegate_transactions")
class DelegateTransaction(
    timestamp: Long,
    amount: Double,
    recipientAddress: String,
    senderAddress: String,
    senderPublicKey: String,
    senderSignature: String,
    hash: String,

    @Column(name = "delegate_key", nullable = false, unique = true)
    var delegateKey: String,

    block: MainBlock? = null

) : BaseTransaction(timestamp, amount, recipientAddress, senderPublicKey, senderAddress, senderSignature, hash, block)