package io.openfuture.chain.domain.crypto.key

data class AddressKeyDto(
    val publicKey: String,
    val privateKey: String? = null,
    val address: String? = null
)