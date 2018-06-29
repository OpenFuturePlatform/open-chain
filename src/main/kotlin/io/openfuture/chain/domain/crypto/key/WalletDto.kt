package io.openfuture.chain.domain.crypto.key

data class WalletDto(
        val seedPhrase: String,
        val publicKey: String,
        val privateKey: String? = null,
        val addressKeyDto: AddressKeyDto
)