package io.openfuture.chain.domain.transaction.data

class DelegateTransactionData(
    amount: Long,
    recipientAddress: String,
    senderAddress: String,
    var delegateKey: String
) : BaseTransactionData(amount, recipientAddress, senderAddress) {

    override fun getBytes(): ByteArray {
        val builder = StringBuilder()
        builder.append(amount)
        builder.append(recipientAddress)
        builder.append(senderAddress)
        builder.append(delegateKey)
        return builder.toString().toByteArray()
    }

}