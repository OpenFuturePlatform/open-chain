package io.openfuture.chain.rpc.domain.transaction

import javax.validation.constraints.NotBlank

class TransferTransactionRequest(
    @field:NotBlank var amount: Long? = null,
    @field:NotBlank var recipientAddress: String? = null
) : BaseTransactionRequest()