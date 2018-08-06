package io.openfuture.chain.rpc.domain.transaction.request.transfer

import io.openfuture.chain.rpc.domain.transaction.request.BaseTransactionHashRequest
import javax.validation.constraints.NotBlank

class TransferTransactionHashRequest(
    timestamp: Long? = null,
    fee: Long? = null,
    senderAddress: String? = null,
    @field:NotBlank var amount: Long? = null,
    @field:NotBlank var recipientAddress: String? = null
) : BaseTransactionHashRequest(timestamp, fee, senderAddress)