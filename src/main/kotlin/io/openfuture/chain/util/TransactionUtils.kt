package io.openfuture.chain.util

import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.domain.transaction.TransactionData

object TransactionUtils {

    fun generateHash(data: TransactionData): String {
        val bytes = getByteData(data)
        return HashUtils.generateHash(bytes)
    }

    fun isValidHash(hash: String, data: TransactionData): Boolean {
        val bytes = getByteData(data)
        return hash == HashUtils.generateHash(bytes)
    }

    private fun getByteData(data: TransactionData): ByteArray {
        val builder = StringBuilder()
        builder.append(data.amount)
        builder.append(data.timestamp)
        builder.append(data.recipientKey)
        builder.append(data.senderKey)
        builder.append(data.signature)
        return builder.toString().toByteArray()
    }

}