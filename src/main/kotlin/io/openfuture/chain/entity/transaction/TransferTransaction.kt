package io.openfuture.chain.entity.transaction

import io.openfuture.chain.domain.transaction.TransferTransactionDto
import io.openfuture.chain.entity.Block
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "transfer_transactions")
class TransferTransaction(
    timestamp: Long,
    amount: Long,
    recipientKey: String,
    recipientAddress: String,
    senderKey: String,
    senderAddress: String,
    senderSignature: String,
    hash: String,
    block: Block? = null
) : BaseTransaction(timestamp, amount, recipientKey, recipientAddress, senderKey, senderAddress, senderSignature,
    hash, block) {

    companion object {
        fun of(dto: TransferTransactionDto): TransferTransaction = TransferTransaction(
            dto.timestamp,
            dto.amount,
            dto.recipientKey,
            dto.recipientAddress,
            dto.senderKey,
            dto.senderAddress,
            dto.senderSignature,
            dto.hash
        )
    }

}