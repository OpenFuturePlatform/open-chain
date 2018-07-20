package io.openfuture.chain.domain.transaction.data

import io.openfuture.chain.domain.delegate.DelegateDto
import javax.validation.constraints.NotNull

class DelegateTransactionData(
    @field:NotNull var delegateDto: DelegateDto? = null
) : BaseTransactionData() {

    override fun getBytes(): ByteArray {
        val builder = StringBuilder()
        builder.append(amount)
        builder.append(recipientAddress)
        builder.append(senderAddress)
        builder.append(delegateDto)
        return builder.toString().toByteArray()
    }

}