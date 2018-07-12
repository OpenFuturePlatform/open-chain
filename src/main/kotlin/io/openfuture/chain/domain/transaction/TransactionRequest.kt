package io.openfuture.chain.domain.transaction

import io.openfuture.chain.crypto.util.HashUtils
import org.jetbrains.annotations.NotNull
import java.util.*
import javax.validation.constraints.NotBlank

class TransactionRequest(
    @NotNull val amount: Int?,
    @NotBlank val recipientKey: String?,
    @NotBlank val senderKey: String?,
    @NotBlank val signature: String?,
    @NotBlank val senderAddress: String?,
    @NotBlank val recipientAddress: String?,
    @NotNull val timestamp: Long? = Date().time
) {

    var hash: String = calculateHash()

    private fun calculateHash(): String {
        val builder = StringBuilder()
        builder.append(this.amount)
        builder.append(this.timestamp)
        builder.append(this.recipientKey)
        builder.append(this.senderKey)
        builder.append(this.signature)
        builder.append(this.senderAddress)
        builder.append(this.recipientAddress)
        return HashUtils.generateHash(builder.toString().toByteArray())
    }
}