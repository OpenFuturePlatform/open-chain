package io.openfuture.chain.domain.transaction.data

class TransferTransactionData : BaseTransactionData() {

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