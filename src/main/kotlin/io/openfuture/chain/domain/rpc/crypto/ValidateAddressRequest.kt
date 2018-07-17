package io.openfuture.chain.domain.rpc.crypto

import io.openfuture.chain.annotation.AddressChecksum
import javax.validation.constraints.NotBlank

data class ValidateAddressRequest(
    @field:NotBlank @field:AddressChecksum var address: String? = null
)