package io.openfuture.chain.domain.rpc.transaction

import io.openfuture.chain.domain.delegate.DelegateInfo
import io.openfuture.chain.entity.dictionary.VoteType
import javax.validation.constraints.NotNull

class VoteTransactionRequest(
    @field:NotNull var voteType: VoteType? = null,
    @field:NotNull var delegateInfo: DelegateInfo? = null
) : TransactionRequest() {

    override fun getBytes(): ByteArray {
        val builder = StringBuilder()
        builder.append(amount)
        builder.append(recipientAddress)
        builder.append(senderKey)
        builder.append(senderAddress)
        builder.append(senderSignature)
        builder.append(voteType)
        builder.append(delegateInfo)
        return builder.toString().toByteArray()
    }

}