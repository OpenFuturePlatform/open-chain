package io.openfuture.chain.entity

import io.openfuture.chain.entity.base.BaseModel
import io.openfuture.chain.entity.dictionary.TransactionType
import io.openfuture.chain.util.DictionaryUtils
import javax.persistence.*

@Entity
@Table(name = "transactions")
@Inheritance(strategy = InheritanceType.JOINED)
abstract class Transaction(

        @Column(name = "type_id", nullable = false, updatable = false)
        var typeId: Int,

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

        @ManyToOne
        @JoinColumn(name = "block_id", nullable = true)
        var block: Block? = null

) : BaseModel() {

        fun getType() = DictionaryUtils.valueOf(TransactionType::class.java, typeId)
}