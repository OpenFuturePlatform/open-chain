package io.openfuture.chain.core.model.entity.transaction.confirmed

import com.fasterxml.jackson.annotation.JsonIgnore
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.Transaction
import io.openfuture.chain.network.message.core.TransactionMessage
import javax.persistence.*

@Entity
@Table(name = "transactions")
@Inheritance(strategy = InheritanceType.JOINED)
abstract class Transaction(
    timestamp: Long,
    fee: Long,
    senderAddress: String,
    hash: String,
    senderSignature: String,
    senderPublicKey: String,

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "block_id", nullable = false)
    var block: MainBlock

) : Transaction(timestamp, fee, senderAddress, hash, senderSignature, senderPublicKey) {

    abstract fun toMessage(): TransactionMessage

}