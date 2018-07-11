package io.openfuture.chain.domain.crypto

import io.openfuture.chain.domain.crypto.key.KeyDto

data class RootAccountDto(
    val seedPhrase: String,
    val masterKeys: KeyDto,
    val defaultAccount: AccountDto
)