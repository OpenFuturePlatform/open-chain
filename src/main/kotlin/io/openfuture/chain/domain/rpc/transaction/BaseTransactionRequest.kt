package io.openfuture.chain.domain.rpc.transaction

import io.openfuture.chain.domain.transaction.data.BaseTransactionData
import javax.validation.constraints.NotBlank

class BaseTransactionRequest<Data : BaseTransactionData>(
    @field:NotBlank var data: Data? = null,
    @field:NotBlank var senderPublicKey: String? = null,
    @field:NotBlank var senderSignature: String? = null
)