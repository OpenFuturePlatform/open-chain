package io.openfuture.chain.domain.transaction.data

abstract class BaseTransactionData(
    val amount: Long,
    val recipientKey: String,
    val recipientAddress: String,
    val senderKey: String,
    val senderAddress: String,
    val senderSignature: String

) {

    abstract fun getByteData(): ByteArray

}