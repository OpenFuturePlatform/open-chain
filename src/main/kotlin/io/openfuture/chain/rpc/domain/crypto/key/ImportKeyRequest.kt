package io.openfuture.chain.rpc.domain.crypto.key

import javax.validation.constraints.NotBlank

data class ImportKeyRequest(
    @field:NotBlank var decodedKey: String? = null
)