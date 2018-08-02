package io.openfuture.chain.rpc.domain.transaction.request

import io.openfuture.chain.core.model.entity.transaction.payload.DelegateTransactionPayload
import io.openfuture.chain.core.model.entity.transaction.unconfirmed.UDelegateTransaction
import io.openfuture.chain.core.util.TransactionUtils
import javax.validation.constraints.NotBlank

class DelegateTransactionRequest(
    @field:NotBlank var fee: Long? = null,
    @field:NotBlank var delegateKey: String? = null
) : BaseTransactionRequest() {

    override fun toUEntity(timestamp: Long): UDelegateTransaction = UDelegateTransaction(
        timestamp,
        senderAddress!!,
        senderPublicKey!!,
        senderSignature!!,
        TransactionUtils.createHash(DelegateTransactionPayload(fee!!, delegateKey!!), senderPublicKey!!, senderSignature!!),
        DelegateTransactionPayload(fee!!, delegateKey!!)
    )

}