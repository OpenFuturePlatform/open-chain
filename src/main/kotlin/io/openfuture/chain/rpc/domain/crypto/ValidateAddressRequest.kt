package io.openfuture.chain.rpc.domain.crypto

import io.openfuture.chain.crypto.annotation.AddressChecksum
import javax.validation.constraints.NotBlank

data class ValidateAddressRequest(
    @field:NotBlank @field:AddressChecksum var address: String? = null
)