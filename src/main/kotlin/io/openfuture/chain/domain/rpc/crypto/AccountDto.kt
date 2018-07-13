package io.openfuture.chain.domain.rpc.crypto

import io.openfuture.chain.domain.rpc.crypto.key.KeyDto

data class AccountDto(
    val seedPhrase: String,
    val masterKeys: KeyDto,
    val defaultWallet: WalletDto
)