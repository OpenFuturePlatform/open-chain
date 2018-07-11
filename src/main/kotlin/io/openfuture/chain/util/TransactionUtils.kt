package io.openfuture.chain.util

import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.domain.transaction.data.BaseTransactionData

object TransactionUtils {

    fun calculateHash(networkTime: Long, data: BaseTransactionData): String {
        val bytes = data.getByteData() + networkTime.toString().toByteArray()
        return HashUtils.generateHash(bytes)
    }

}