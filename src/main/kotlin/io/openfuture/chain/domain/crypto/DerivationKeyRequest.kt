package io.openfuture.chain.domain.crypto

data class DerivationKeyRequest(
        val seedPhrase: String,
        val derivationPath: String
)