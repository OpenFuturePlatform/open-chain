package io.openfuture.chain.rpc.domain.transaction

import javax.validation.constraints.NotBlank

class DelegateTransactionRequest(
    @field:NotBlank var delegateKey: String? = null
) : BaseTransactionRequest()