package io.openfuture.chain.core.model.entity.transaction.confirmed

import io.openfuture.chain.core.model.dto.transaction.TransferTransactionDto
import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.transaction.payload.TransferTransactionPayload
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "transfer_transactions")
class TransferTransaction(
    timestamp: Long,
    senderAddress: String,
    senderPublicKey: String,
    senderSignature: String,
    hash: String,

    @Embedded
    private var payload: TransferTransactionPayload,

    block: MainBlock? = null
) : Transaction(timestamp, senderAddress, senderPublicKey, senderSignature, hash, block) {

    override fun getPayload(): TransferTransactionPayload {
        return payload
    }

    override fun toMessage(): TransferTransactionDto = TransferTransactionDto(
        timestamp,
        payload.fee,
        senderAddress,
        senderPublicKey,
        senderSignature,
        hash,
        payload.amount,
        payload.recipientAddress
    )

}