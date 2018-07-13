package io.openfuture.chain.domain.rpc.crypto.key

data class KeyDto(
    val publicKey: String,
    val privateKey: String? = null
)