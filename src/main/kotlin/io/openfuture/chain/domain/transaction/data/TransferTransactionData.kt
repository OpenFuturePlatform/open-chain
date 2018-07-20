package io.openfuture.chain.domain.transaction.data

class TransferTransactionData : BaseTransactionData() {

    override fun getBytes(): ByteArray {
        val builder = StringBuilder()
        builder.append(amount)
        builder.append(recipientAddress)
        builder.append(senderAddress)
        return builder.toString().toByteArray()
    }

}