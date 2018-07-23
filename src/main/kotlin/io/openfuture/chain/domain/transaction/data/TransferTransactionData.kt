package io.openfuture.chain.domain.transaction.data

class TransferTransactionData(
    amount: Double,
    recipientAddress: String,
    senderAddress: String
) : BaseTransactionData(amount, recipientAddress, senderAddress) {

    override fun getBytes(): ByteArray {
        val builder = StringBuilder()
        builder.append(amount)
        builder.append(recipientAddress)
        builder.append(senderAddress)
        return builder.toString().toByteArray()
    }

}