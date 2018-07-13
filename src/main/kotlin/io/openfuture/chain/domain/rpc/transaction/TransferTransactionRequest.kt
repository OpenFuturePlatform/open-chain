package io.openfuture.chain.domain.rpc.transaction

class TransferTransactionRequest : TransactionRequest() {

    override fun getBytes(): ByteArray {
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