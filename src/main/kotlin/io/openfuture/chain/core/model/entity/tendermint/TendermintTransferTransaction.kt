package io.openfuture.chain.core.model.entity.tendermint

import io.openfuture.chain.core.model.entity.dictionary.TransferTransactionType
import io.openfuture.chain.core.model.entity.transaction.payload.TransferTransactionPayload
import io.openfuture.chain.network.message.core.TransferTransactionMessage
import io.openfuture.chain.tendermint.domain.TendermintTransactionRequest
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "tendermint_transfer_transactions")
class TendermintTransferTransaction (
    timestamp: Long,
    fee: Long,
    senderAddress: String,
    hash: String,
    signature: String,
    publicKey: String,
    @Embedded
    private var payload: TransferTransactionPayload
) : TendermintTransaction(timestamp, fee, senderAddress, hash, signature, publicKey) {

    companion object {

        fun of(request: TendermintTransactionRequest): TendermintTransferTransaction = TendermintTransferTransaction(
            request.timestamp!!, request.fee!!, request.senderAddress!!, request.hash!!, request.senderSignature!!,
            request.senderPublicKey!!, TransferTransactionPayload(request.amount!!, request.recipientAddress)
        )
    }


    fun getType(): TransferTransactionType = TransferTransactionType.getType(payload.recipientAddress, payload.data)

    override fun toMessage(): TransferTransactionMessage = TransferTransactionMessage(
        timestamp, fee, senderAddress, hash, signature, publicKey, payload.amount, payload.recipientAddress,
        payload.data
    )

    override fun getPayload(): TransferTransactionPayload = payload

}