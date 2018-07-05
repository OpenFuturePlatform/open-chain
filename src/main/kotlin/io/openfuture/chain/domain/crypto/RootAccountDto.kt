package io.openfuture.chain.domain.crypto

data class RootAccountDto(
    val seedPhrase: String,
    val masterPublicKey: String,
    val masterPrivateKey: String? = null,
    val defaultAccount: AccountDto
)