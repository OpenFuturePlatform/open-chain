package io.openfuture.chain.entity.transaction

import com.fasterxml.jackson.annotation.JsonIgnore
import io.openfuture.chain.entity.block.MainBlock
import io.openfuture.chain.entity.transaction.base.BaseTransaction
import javax.persistence.*

@Entity
@Table(name = "transactions")
@Inheritance(strategy = InheritanceType.JOINED)
abstract class Transaction(
    timestamp: Long,
    amount: Long,
    recipientAddress: String,
    senderAddress: String,
    senderPublicKey: String,
    senderSignature: String,
    hash: String,

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "block_id", nullable = true)
    var block: MainBlock? = null

) : BaseTransaction(timestamp, amount, recipientAddress, senderAddress, senderPublicKey, senderSignature, hash)