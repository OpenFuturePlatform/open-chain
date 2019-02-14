package io.openfuture.chain.core.model.entity.transaction.confirmed

import io.openfuture.chain.core.model.entity.block.MainBlock
import io.openfuture.chain.core.model.entity.dictionary.TransferTransactionType
import io.openfuture.chain.core.model.entity.transaction.payload.TransferTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UnconfirmedTransferTransaction
import io.openfuture.chain.network.message.core.TransferTransactionMessage
import javax.persistence.Embedded
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "transfer_transactions")
class TransferTransaction(
    timestamp: Long,
    fee: Long,
    senderAddress: String,
    hash: String,
    signature: String,
    publicKey: String,

    @Embedded
    private val payload: TransferTransactionPayload,

    block: MainBlock? = null
) : Transaction(timestamp, fee, senderAddress, hash, signature, publicKey, block) {

    companion object {
        fun of(message: TransferTransactionMessage, block: MainBlock? = null): TransferTransaction = TransferTransaction(
            message.timestamp, message.fee, message.senderAddress, message.hash, message.signature, message.publicKey,
            TransferTransactionPayload(message.amount, message.recipientAddress, message.data), block
        )

        fun of(utx: UnconfirmedTransferTransaction, block: MainBlock? = null): TransferTransaction = TransferTransaction(
            utx.timestamp, utx.fee, utx.senderAddress, utx.hash, utx.signature, utx.publicKey, utx.getPayload(), block
        )
    }


    fun getType(): TransferTransactionType = TransferTransactionType.getType(payload.recipientAddress, payload.data)

    override fun toMessage(): TransferTransactionMessage = TransferTransactionMessage(
        timestamp, fee, senderAddress, hash, signature, publicKey, payload.amount, payload.recipientAddress,
        payload.data
    )

    override fun getPayload(): TransferTransactionPayload = payload

}