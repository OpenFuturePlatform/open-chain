package io.openfuture.chain.domain.transaction.data

import io.openfuture.chain.crypto.util.HashUtils
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

abstract class BaseTransactionData(
    @field:NotNull var amount: Double? = null,
    @field:NotBlank var recipientAddress: String? = null,
    @field:NotBlank var senderAddress: String? = null
) {

    abstract fun getBytes(): ByteArray

    fun getHash(): String = HashUtils.toHexString(HashUtils.sha256(getBytes()))

}