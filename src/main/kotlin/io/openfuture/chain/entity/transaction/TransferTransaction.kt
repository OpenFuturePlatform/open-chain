package io.openfuture.chain.entity.transaction

import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.domain.rpc.transaction.TransferTransactionRequest
import io.openfuture.chain.domain.transaction.TransferTransactionDto
import io.openfuture.chain.entity.MainBlock
import io.openfuture.chain.network.domain.NetworkTransferTransaction
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
    hash: String? = null,
    senderSignature: String? = null,
    block: MainBlock? = null
) : BaseTransaction(timestamp, amount, fee, recipientAddress, senderKey, senderAddress,
    hash ?: HashUtils.toHexString(
        HashUtils.sha256((senderAddress + recipientAddress + timestamp + amount + fee).toByteArray())),
    senderSignature, block) {

    companion object {
        fun of(dto: TransferTransactionDto): TransferTransaction = TransferTransaction(
            dto.timestamp,
            dto.amount,
            dto.fee,
            dto.recipientAddress,
            dto.senderKey,
            dto.senderAddress,
            dto.hash,
            dto.senderSignature
        )

        fun of(timestamp: Long, request: TransferTransactionRequest): TransferTransaction = TransferTransaction(
            timestamp,
            request.amount!!,
            request.fee!!,
            request.recipientAddress!!,
            request.senderKey!!,
            request.senderAddress!!,
            request.getHash(),
            request.senderSignature!!
        )

        fun of(dto: NetworkTransferTransaction): TransferTransaction = TransferTransaction(
            dto.timestamp,
            dto.amount,
            dto.fee,
            dto.recipientAddress,
            dto.senderKey,
            dto.senderAddress,
            null,
            dto.senderSignature
        )

    }

}