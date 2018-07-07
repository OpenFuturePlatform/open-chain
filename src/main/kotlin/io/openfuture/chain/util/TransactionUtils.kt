package io.openfuture.chain.util

import io.openfuture.chain.crypto.util.HashUtils
import io.openfuture.chain.domain.transaction.data.TransactionData

object TransactionUtils {

    fun calculateHash(data: TransactionData): String {
        val bytes = data.getByteData()
        return HashUtils.generateHash(bytes)
    }

}