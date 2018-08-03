package io.openfuture.chain.core.model.entity.transaction.confirmed

import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.BaseTransaction
import io.openfuture.chain.core.model.entity.transaction.payload.BaseTransactionPayload
import javax.persistence.*

@Entity
@Table(name = "transactions")
@Inheritance(strategy = InheritanceType.JOINED)
abstract class Transaction(
    timestamp: Long,
    senderAddress: String,
    senderPublicKey: String,
    senderSignature: String,
    hash: String,
    payload: BaseTransactionPayload,

    @ManyToOne
    @JoinColumn(name = "block_id", nullable = false)
    var block: MainBlock? = null

) : BaseTransaction(timestamp, senderAddress, senderPublicKey, senderSignature, hash, payload)