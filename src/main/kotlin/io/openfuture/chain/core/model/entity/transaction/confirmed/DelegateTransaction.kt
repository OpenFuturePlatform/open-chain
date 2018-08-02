package io.openfuture.chain.core.model.entity.transaction.confirmed

import io.openfuture.chain.core.model.dto.transaction.DelegateTransactionDto
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.payload.BaseTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.payload.DelegateTransactionPayload
import javax.persistence.Column
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "delegate_transactions")
class DelegateTransaction(
    timestamp: Long,
    senderAddress: String,
    senderPublicKey: String,
    senderSignature: String,
    hash: String,

    @Embedded
    private var payload: DelegateTransactionPayload,

    block: MainBlock? = null

) : Transaction(timestamp, senderPublicKey, senderAddress, senderSignature, hash, block) {

    override fun getPayload(): DelegateTransactionPayload {
        return payload
    }

    override fun toMessage(): DelegateTransactionDto = DelegateTransactionDto(
        timestamp,
        payload.fee,
        senderAddress,
        senderPublicKey,
        senderSignature,
        hash,
        payload.delegateKey
    )

}