package io.openfuture.chain.entity

import io.openfuture.chain.domain.transaction.TransactionRequest
import io.openfuture.chain.entity.base.BaseModel
import javax.persistence.*

/**
 * @author Homza Pavel
 */
@Entity
@Table(name = "transactions")
class Transaction(

    @ManyToOne
    @JoinColumn(name = "block_id", nullable = false)
    val block: Block,

    @Column(name = "hash", nullable = false)
    val hash: String,

    @Column(name = "amount", nullable = false)
    val amount: Int = 0,

    @Column(name = "timestamp", nullable = false)
    val timestamp: Long,

    @Column(name = "recipient_key", nullable = false)
    val recipientkey: String,

    @Column(name = "sender_key", nullable = false)
    val senderKey: String,

    @Column(name = "signature", nullable = false)
    val signature: String

) : BaseModel() {

    companion object {
        fun of(block: Block, request: TransactionRequest): Transaction = Transaction(
            block,
            request.hash,
            request.amount,
            request.timestamp,
            request.recipientkey,
            request.senderKey,
            request.signature
        )
    }
}