package io.openfuture.chain.rpc.domain.transaction.request.delegate

import io.openfuture.chain.rpc.domain.transaction.request.BaseTransactionHashRequest
import javax.validation.constraints.NotBlank

class DelegateTransactionHashRequest(
    timestamp: Long? = null,
    fee: Long? = null,
    @field:NotBlank var delegateKey: String? = null
) : BaseTransactionHashRequest(timestamp, fee)