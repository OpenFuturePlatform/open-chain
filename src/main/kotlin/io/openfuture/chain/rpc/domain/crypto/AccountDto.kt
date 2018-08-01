package io.openfuture.chain.rpc.domain.crypto

import io.openfuture.chain.rpc.domain.crypto.key.KeyDto

data class AccountDto(
    val seedPhrase: String,
    val masterKeys: KeyDto,
    val defaultWallet: WalletDto
)