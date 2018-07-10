package io.openfuture.chain.domain.crypto

import io.openfuture.chain.annotation.Address
import javax.validation.constraints.NotBlank

data class ValidateAddressRequest(
    @field:NotBlank @field:Address var address: String? = null
)