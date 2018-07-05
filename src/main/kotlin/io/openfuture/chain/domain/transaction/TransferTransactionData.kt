package io.openfuture.chain.domain.transaction

class TransferTransactionData(
        val amount: Long,
        val recipientKey: String,
        val senderKey: String,
        val senderSignature: String
) {

    private fun getByteData(): ByteArray {
        val builder = StringBuilder()
        builder.append(amount)
        builder.append(recipientKey)
        builder.append(senderKey)
        builder.append(senderSignature)
        return builder.toString().toByteArray()
    }

}