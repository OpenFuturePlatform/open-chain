package io.openfuture.chain.domain.transaction.data

class TransferTransactionData(
    amount: Long,
    recipientKey: String,
    senderKey: String,
    senderSignature: String
) : BaseTransactionData(amount, recipientKey, senderKey, senderSignature) {

    override fun getByteData(): ByteArray {
        val builder = StringBuilder()
        builder.append(amount)
        builder.append(recipientKey)
        builder.append(senderKey)
        builder.append(senderSignature)
        return builder.toString().toByteArray()
    }

}