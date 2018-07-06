package io.openfuture.chain.entity

import io.openfuture.chain.domain.transaction.TransactionDto
import io.openfuture.chain.domain.transaction.payload.TransactionPayload
import io.openfuture.chain.entity.base.BaseModel
import io.openfuture.chain.util.JsonUtils.Companion.fromJson
import io.openfuture.chain.util.JsonUtils.Companion.toJson
import javax.persistence.*

@Entity
@Table(name = "transactions")
class Transaction(

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

        @Access(AccessType.PROPERTY)
        @Column(name = "payload", nullable = true)
        private var payloadJson: String?,

        @Transient
        private var payload: TransactionPayload?,

        @ManyToOne
        @JoinColumn(name = "block_id", nullable = true)
        var block: Block? = null

) : BaseModel() {

    companion object {
        fun of(dto: TransactionDto): Transaction = Transaction(
                dto.timestamp,
                dto.data.amount,
                dto.data.recipientKey,
                dto.data.senderKey,
                dto.data.senderSignature,
                dto.hash,
                toJson(dto.data.payload),
                dto.data.payload
        )
    }

    fun setPayloadJson(payloadJson: String) {
        this.payloadJson = payloadJson
        this.payload = fromJson(payloadJson, TransactionPayload::class.java)
    }

    fun setPayload(payload: TransactionPayload) {
        this.payload = payload
        this.payloadJson = toJson(payload)
    }

    fun getPayloadJson(): String? {
        return payloadJson
    }

    fun getPayload(): TransactionPayload? {
        return payload
    }

    @PostLoad
    fun initFields() {
        this.payload = fromJson(payloadJson, TransactionPayload::class.java)
    }

}