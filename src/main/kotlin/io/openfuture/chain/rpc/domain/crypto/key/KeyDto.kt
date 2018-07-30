package io.openfuture.chain.rpc.domain.crypto.key

data class KeyDto(
    val publicKey: String,
    val privateKey: String? = null
)