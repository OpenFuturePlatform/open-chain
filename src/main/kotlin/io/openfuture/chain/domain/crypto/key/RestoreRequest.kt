package io.openfuture.chain.domain.crypto.key

import javax.validation.constraints.NotBlank

data class RestoreRequest(
    @field:NotBlank var seedPhrase: String? = null
)