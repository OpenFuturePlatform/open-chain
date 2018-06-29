package io.openfuture.chain.domain.crypto.key

data class WalletDto(
        val seedPhrase: String,
        val masterPublicKey: String,
        val masterPrivateKey: String? = null,
        val addressKeyDto: AddressKeyDto
)