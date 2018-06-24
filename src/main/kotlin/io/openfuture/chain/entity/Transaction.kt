package io.openfuture.chain.entity

import io.openfuture.chain.domain.transaction.TransactionDto
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
    var recipientkey: String,

    @Column(name = "sender_key", nullable = false)
    var senderKey: String,

    @Column(name = "signature", nullable = false)
    var signature: String

) : BaseModel() {

    companion object {
        fun of(block: Block, dto: TransactionDto): Transaction = Transaction(
            block,
            dto.hash,
            dto.amount,
            dto.timestamp,
            dto.recipientKey,
            dto.senderKey,
            dto.signature
        )
    }
}