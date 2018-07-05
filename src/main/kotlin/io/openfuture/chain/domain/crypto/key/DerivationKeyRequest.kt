package io.openfuture.chain.domain.crypto.key

data class DerivationKeyRequest(
    val seedPhrase: String,
    val derivationPath: String
)