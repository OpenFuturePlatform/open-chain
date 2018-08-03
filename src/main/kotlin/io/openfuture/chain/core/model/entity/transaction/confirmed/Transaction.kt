package io.openfuture.chain.core.model.entity.transaction.confirmed

import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.BaseTransaction
import io.openfuture.chain.core.model.entity.transaction.payload.BaseTransactionPayload
import javax.persistence.*

@Entity
@Table(name = "transactions")
@Inheritance(strategy = InheritanceType.JOINED)
abstract class Transaction<T : BaseTransactionPayload>(
    timestamp: Long,
    payload: T,
    senderAddress: String,
    senderPublicKey: String,
    senderSignature: String,
    hash: String,

    @ManyToOne
    @JoinColumn(name = "hash", nullable = false)
    var block: MainBlock? = null

) : BaseTransaction<T>(timestamp, payload, senderAddress, senderPublicKey, senderSignature, hash)