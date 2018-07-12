package io.openfuture.chain.entity

import io.openfuture.chain.domain.transaction.TransactionRequest
import io.openfuture.chain.entity.base.BaseModel
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "transactions")
class Transaction(

    @Column(name = "hash", nullable = false)
    var hash: String,

    @Column(name = "amount", nullable = false)
    var amount: Int,

    @Column(name = "timestamp", nullable = false)
    var timestamp: Long,

    @Column(name = "recipient_key", nullable = false)
    var recipientKey: String,

    @Column(name = "sender_key", nullable = false)
    var senderKey: String,

    @Column(name = "signature", nullable = false)
    var signature: String,

    @Column(name = "sender_address", nullable = false)
    var senderAddress: String,

    @Column(name = "recipient_address", nullable = false)
    var recipientAddress: String,

    @Column(name = "block_hash", nullable = true)
    var blockHash: String? = null

) : BaseModel() {

    companion object {
        fun of(request: TransactionRequest): Transaction = Transaction(
            request.hash,
            request.amount!!,
            request.timestamp!!,
            request.recipientKey!!,
            request.senderKey!!,
            request.signature!!,
            request.senderAddress!!,
            request.recipientAddress!!
        )
    }
}