package io.openfuture.chain.rpc.domain.transaction.request.transfer

import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

data class TransferTransactionHashRequest(
    @field:NotNull var timestamp: Long? = null,
    @field:NotNull var fee: Long? = null,
    @field:NotBlank var senderAddress: String? = null,
    @field:NotNull var amount: Long? = null,
    @field:NotBlank var recipientAddress: String? = null
)