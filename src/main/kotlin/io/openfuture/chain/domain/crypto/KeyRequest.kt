package io.openfuture.chain.domain.crypto

data class KeyRequest(
        val seedPhrase: String,
        val derivationPath: String
)