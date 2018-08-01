package io.openfuture.chain.core.model.dto.transaction.data

import io.openfuture.chain.consensus.annotation.NoArgConstructor

@NoArgConstructor
class TransferTransactionData(
    amount: Long,
    fee: Long,
    recipientAddress: String,
    senderAddress: String
) : BaseTransactionData(amount, fee, recipientAddress, senderAddress) {

    override fun getBytes(): ByteArray {
        val builder = StringBuilder()
        builder.append(amount)
        builder.append(fee)
        builder.append(recipientAddress)
        builder.append(senderAddress)
        return builder.toString().toByteArray()
    }

}