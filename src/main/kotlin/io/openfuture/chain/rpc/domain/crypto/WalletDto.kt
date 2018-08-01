package io.openfuture.chain.rpc.domain.crypto

import io.openfuture.chain.crypto.model.dto.ECKey
import io.openfuture.chain.rpc.domain.crypto.key.KeyDto

data class WalletDto(
    val keys: KeyDto,
    val address: String? = null
) {

    constructor(ecKey: ECKey) : this(KeyDto(ecKey), ecKey.getAddress())

}