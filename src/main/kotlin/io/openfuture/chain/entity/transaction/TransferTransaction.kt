package io.openfuture.chain.entity.transaction

import io.openfuture.chain.domain.rpc.transaction.TransferTransactionRequest
import io.openfuture.chain.domain.transaction.TransferTransactionDto
import io.openfuture.chain.entity.MainBlock
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "transfer_transactions")
class TransferTransaction(
    timestamp: Long,
    amount: Double,
    fee: Double,
    recipientAddress: String,
    senderKey: String,
    senderAddress: String,
    senderSignature: String,
    hash: String,
    block: MainBlock? = null
) : BaseTransaction(timestamp, amount, fee, recipientAddress, senderKey, senderAddress, senderSignature, hash, block) {

    companion object {
        fun of(dto: TransferTransactionDto): TransferTransaction = TransferTransaction(
            dto.timestamp,
            dto.amount,
            dto.fee,
            dto.recipientAddress,
            dto.senderKey,
            dto.senderAddress,
            dto.senderSignature,
            dto.hash
        )

        fun of(timestamp: Long, request: TransferTransactionRequest): TransferTransaction = TransferTransaction(
            timestamp,
            request.amount!!,
            request.fee!!,
            request.recipientAddress!!,
            request.senderKey!!,
            request.senderAddress!!,
            request.senderSignature!!,
            request.getHash()
        )
    }

}