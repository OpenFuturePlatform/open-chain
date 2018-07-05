package io.openfuture.chain.domain.crypto

import io.openfuture.chain.domain.crypto.key.AddressKeyDto

data class AccountDto(
    val seedPhrase: String,
    val masterPublicKey: String,
    val masterPrivateKey: String? = null,
    val addressKeyDto: AddressKeyDto
)