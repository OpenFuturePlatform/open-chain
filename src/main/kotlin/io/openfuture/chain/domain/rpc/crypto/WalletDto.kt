package io.openfuture.chain.domain.rpc.crypto

import io.openfuture.chain.crypto.domain.ECKey
import io.openfuture.chain.domain.rpc.crypto.key.KeyDto
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