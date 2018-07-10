package io.openfuture.chain.entity.transaction

import com.fasterxml.jackson.annotation.JsonIgnore
import io.openfuture.chain.domain.transaction.TransactionDto
import io.openfuture.chain.entity.base.BaseModel
import io.openfuture.chain.entity.block.Block
import javax.persistence.*

@Entity
@Table(name = "transactions")
@Inheritance(strategy = InheritanceType.JOINED)
open class Transaction(

        @Column(name = "timestamp", nullable = false)
        var timestamp: Long,

        @Column(name = "amount", nullable = false)
        var amount: Long,

        @Column(name = "recipient_key", nullable = false)
        var recipientKey: String,

        @Column(name = "sender_key", nullable = false)
        var senderKey: String,

        @Column(name = "sender_signature", nullable = false)
        var senderSignature: String,

        @Column(name = "hash", nullable = false)
        var hash: String,

        @JsonIgnore
        @ManyToOne
        @JoinColumn(name = "block_id", nullable = true)
        var block: Block? = null

) : BaseModel() {

    companion object {
        fun of(dto: TransactionDto): Transaction = Transaction(
                dto.timestamp,
                dto.amount,
                dto.recipientKey,
                dto.senderKey,
                dto.senderSignature,
                dto.hash
        )
    }

}