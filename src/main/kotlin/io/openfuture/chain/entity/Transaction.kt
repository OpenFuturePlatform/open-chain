package io.openfuture.chain.entity

import io.openfuture.chain.domain.transaction.TransactionRequest
import io.openfuture.chain.entity.base.BaseModel
import javax.persistence.*

@Entity
@Table(name = "transactions")
class Transaction(

    @ManyToOne
    @JoinColumn(name = "block_id", nullable = false)
    var block: Block,

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

    @Column(name = "from", nullable = false)
    var from: String,

    @Column(name = "to", nullable = false)
    var to: String

) : BaseModel() {

    companion object {
        fun of(block: Block, request: TransactionRequest): Transaction = Transaction(
            block,
            request.hash,
            request.amount,
            request.timestamp,
            request.recipientKey,
            request.senderKey,
            request.signature,
            request.from,
            request.to
        )
    }
}