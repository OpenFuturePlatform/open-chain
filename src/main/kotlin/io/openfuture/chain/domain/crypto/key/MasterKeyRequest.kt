package io.openfuture.chain.domain.crypto.key

import javax.validation.constraints.NotBlank

data class MasterKeyRequest(
    @field:NotBlank var seedPhrase: String? = null
)