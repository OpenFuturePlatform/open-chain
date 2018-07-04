package io.openfuture.chain.domain.crypto.key

import javax.validation.constraints.NotBlank

data class ImportKeyRequest(
    @field:NotBlank var decodedKey: String? = null
)