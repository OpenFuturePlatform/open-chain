package io.openfuture.chain.rpc.domain.transaction.request.transfer

import io.openfuture.chain.rpc.domain.transaction.request.BaseTransactionRequest
import javax.validation.constraints.NotBlank

class TransferTransactionRequest(
    timestamp: Long,
    fee: Long,
    senderAddress: String,
    senderSignature: String,
    senderPublicKey: String,
    @field:NotBlank var amount: Long? = null,
    @field:NotBlank var recipientAddress: String? = null
) : BaseTransactionRequest(timestamp, fee, senderAddress, senderSignature, senderPublicKey)