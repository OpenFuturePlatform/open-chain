package io.openfuture.chain.domain.crypto.key

import io.openfuture.chain.crypto.domain.ECKey
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils

data class AddressKeyDto(
    val publicKey: String,
    val privateKey: String? = null,
    val address: String? = null
) {

    constructor(ecKey: ECKey) : this(
        ByteUtils.toHexString(ecKey.public),
        ByteUtils.toHexString(ecKey.getPrivate()),
        ecKey.getAddress()
    )

}