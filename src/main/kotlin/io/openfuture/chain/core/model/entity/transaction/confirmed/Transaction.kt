package io.openfuture.chain.core.model.entity.transaction

import io.openfuture.chain.core.model.entity.block.MainBlock
import javax.persistence.*

@Entity
@Table(name = "transactions")
@Inheritance(strategy = InheritanceType.JOINED)
abstract class Transaction(
    timestamp: Long,
    amount: Long,
    fee: Long,
    recipientAddress: String,
    senderAddress: String,
    senderPublicKey: String,
    senderSignature: String,
    hash: String,

    @ManyToOne
    @JoinColumn(name = "block_id", nullable = false)
    var block: MainBlock? = null

) : BaseTransaction(timestamp, amount, fee, recipientAddress, senderAddress, senderPublicKey, senderSignature, hash)