package io.openfuture.chain.domain.crypto

import io.openfuture.chain.crypto.domain.ECKey
import io.openfuture.chain.domain.crypto.key.KeyDto
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils

data class AccountDto(
    val keys: KeyDto,
    val address: String? = null
) {

    constructor(ecKey: ECKey) : this(
        KeyDto(ByteUtils.toHexString(ecKey.public), ByteUtils.toHexString(ecKey.getPrivate())),
        ecKey.getAddress()
    )

}