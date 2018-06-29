package io.openfuture.chain.domain.crypto

import io.openfuture.chain.domain.crypto.key.KeyDto

data class WalletDto(
    val keyDto: KeyDto,
    val address: String
) {

    constructor(publicKey: String, privateKey: String, address: String) : this(KeyDto(publicKey, privateKey), address)

}