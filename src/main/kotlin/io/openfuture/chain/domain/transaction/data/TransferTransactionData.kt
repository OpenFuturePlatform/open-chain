package io.openfuture.chain.domain.transaction.data

class TransferTransactionData(
    amount: Long,
    recipientKey: String,
    recipientAddress: String,
    senderKey: String,
    senderAddress: String,
    senderSignature: String
) : BaseTransactionData(amount, recipientKey, recipientAddress, senderKey, senderAddress, senderSignature) {

    override fun getByteData(): ByteArray {
        val builder = StringBuilder()
        builder.append(amount)
        builder.append(recipientKey)
        builder.append(recipientAddress)
        builder.append(senderKey)
        builder.append(senderAddress)
        builder.append(senderSignature)
        return builder.toString().toByteArray()
    }

}