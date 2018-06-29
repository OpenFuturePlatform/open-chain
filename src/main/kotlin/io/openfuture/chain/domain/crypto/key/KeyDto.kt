package io.openfuture.chain.domain.crypto.key

data class KeyDto(
    val publicKey: String,
    val privateKey: String? = null
)
