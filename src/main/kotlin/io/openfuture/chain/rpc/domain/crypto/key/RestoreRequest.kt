package io.openfuture.chain.rpc.domain.crypto.key

import javax.validation.constraints.NotBlank

data class RestoreRequest(
    @field:NotBlank var seedPhrase: String? = null
)