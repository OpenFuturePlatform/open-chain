package io.openfuture.chain.domain.transaction.data

import io.openfuture.chain.crypto.util.HashUtils

abstract class BaseTransactionData(
    var amount: Double,
    var fee: Double,
    var recipientAddress: String,
    var senderAddress: String
) {

    abstract fun getBytes(): ByteArray

    fun getHash(): String = HashUtils.toHexString(HashUtils.sha256(getBytes()))

}