package io.openfuture.chain.rpc.domain.crypto

import io.openfuture.chain.crypto.model.dto.ECKey
import io.openfuture.chain.rpc.domain.crypto.key.KeyDto
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils

data class WalletDto(
    val keys: KeyDto,
    val address: String? = null
) {

    constructor(ecKey: ECKey) : this(
        KeyDto(ByteUtils.toHexString(ecKey.public), ByteUtils.toHexString(ecKey.getPrivate())),
        ecKey.getAddress()
    )

}