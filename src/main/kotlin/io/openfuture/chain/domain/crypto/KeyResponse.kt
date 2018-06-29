package io.openfuture.chain.domain.crypto

data class KeyResponse(
        val publicKey: String,
        val privateKey: String,
        val address: String
)