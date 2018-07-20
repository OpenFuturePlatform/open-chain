package io.openfuture.chain.domain.rpc.transaction

import io.openfuture.chain.domain.delegate.DelegateDto
import io.openfuture.chain.entity.dictionary.VoteType
import javax.validation.constraints.NotNull

class DelegateTransactionRequest(
    @field:NotNull var voteType: VoteType? = null,
    @field:NotNull var delegateDto: DelegateDto? = null
) : BaseTransactionRequest() {

    override fun getBytes(): ByteArray {
        val builder = StringBuilder()
        builder.append(amount)
        builder.append(recipientAddress)
        builder.append(senderKey)
        builder.append(senderAddress)
        builder.append(senderSignature)
        builder.append(voteType)
        builder.append(delegateDto)
        return builder.toString().toByteArray()
    }

}