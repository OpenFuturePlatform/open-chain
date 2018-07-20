package io.openfuture.chain.domain.transaction.data

import io.openfuture.chain.entity.dictionary.VoteType
import javax.validation.constraints.NotNull

class VoteTransactionData(
    @field:NotNull var voteType: VoteType? = null,
    @field:NotNull var delegateKey: String? = null
) : BaseTransactionData() {

    override fun getBytes(): ByteArray {
        val builder = StringBuilder()
        builder.append(amount)
        builder.append(recipientAddress)
        builder.append(senderAddress)
        builder.append(voteType)
        builder.append(delegateKey)
        return builder.toString().toByteArray()
    }

}