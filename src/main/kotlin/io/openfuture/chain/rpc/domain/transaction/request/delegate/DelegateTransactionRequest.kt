package io.openfuture.chain.rpc.domain.transaction.request.delegate

import io.openfuture.chain.rpc.domain.transaction.request.BaseTransactionRequest
import javax.validation.constraints.NotBlank

class DelegateTransactionRequest(
    timestamp: Long,
    fee: Long,
    senderAddress: String,
    senderSignature: String,
    senderPublicKey: String,
    @field:NotBlank var delegateKey: String? = null
) : BaseTransactionRequest(timestamp, fee, senderAddress, senderSignature, senderPublicKey)