package io.openfuture.chain.util

import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.domain.transaction.data.TransactionData

object TransactionUtils {

    fun calculateHash(networkTime: Long, data: TransactionData): String {
        val bytes = data.getByteData() + networkTime.toString().toByteArray()
        return HashUtils.generateHash(bytes)
    }

}