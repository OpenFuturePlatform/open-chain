package io.openfuture.chain.core.model.entity.transaction.unconfirmed

import io.openfuture.chain.core.model.entity.transaction.payload.TransactionPayload
import io.openfuture.chain.core.model.entity.transaction.payload.TransferTransactionPayload
import io.openfuture.chain.core.util.TransactionUtils
import io.openfuture.chain.network.message.core.TransferTransactionMessage
import io.openfuture.chain.rpc.domain.transaction.request.transfer.TransferTransactionRequest
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "u_transfer_transactions")
class UnconfirmedTransferTransaction(
    timestamp: Long,
    fee: Long,
    hash: String,
    senderAddress: String,
    senderSignature: String,
    senderPublicKey: String,

    @Embedded
    var payload: TransferTransactionPayload

) : UnconfirmedTransaction(timestamp, fee, senderAddress, hash, senderSignature, senderPublicKey) {

    companion object {
        fun of(dto: TransferTransactionMessage): UnconfirmedTransferTransaction = UnconfirmedTransferTransaction(
            dto.timestamp,
            dto.fee,
            dto.senderAddress,
            dto.hash,
            dto.senderSignature,
            dto.senderPublicKey,
            TransferTransactionPayload(dto.amount, dto.recipientAddress)
        )

        fun of(time: Long, request: TransferTransactionRequest): UnconfirmedTransferTransaction = UnconfirmedTransferTransaction(
            time,
            request.fee!!,
            request.senderAddress!!,
            TransactionUtils.generateHash(time, request.fee!!, request.senderAddress!!,
                TransferTransactionPayload(request.amount!!, request.recipientAddress!!)),
            request.senderSignature!!,
            request.senderPublicKey!!,
            TransferTransactionPayload(request.amount!!, request.recipientAddress!!)
        )
    }

    override fun getPayload(): TransactionPayload {
        return payload
    }

}