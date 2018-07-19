package io.openfuture.chain.entity.transaction

import com.fasterxml.jackson.annotation.JsonIgnore
import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.entity.base.BaseModel
import io.openfuture.chain.entity.Block
import io.openfuture.chain.entity.MainBlock
import javax.persistence.*

@Entity
@Table(name = "transactions")
@Inheritance(strategy = InheritanceType.JOINED)
abstract class BaseTransaction(

    @Column(name = "timestamp", nullable = false)
    var timestamp: Long,

    @Column(name = "amount", nullable = false)
    var amount: Double,

    @Column(name = "fee", nullable = false)
    var fee: Double,

    @Column(name = "recipient_address", nullable = false)
    var recipientAddress: String,

    @Column(name = "sender_key", nullable = false)
    var senderKey: String,

    @Column(name = "sender_address", nullable = false)
    var senderAddress: String,

    @Column(name = "sender_signature", nullable = false)
    var senderSignature: String,

    @Column(name = "hash", nullable = false)
    var hash: String,

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "block_id", nullable = true)
    var block: MainBlock? = null

) : BaseModel()