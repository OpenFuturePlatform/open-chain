package io.openfuture.chain.rpc.domain.crypto.key

import io.openfuture.chain.crypto.model.dto.ECKey
import org.bouncycastle.pqc.math.linearalgebra.ByteUtils

data class KeyDto(
    val publicKey: String,
    val privateKey: String? = null
) {

    constructor(ecKey: ECKey) : this(
        ByteUtils.toHexString(ecKey.public),
        ByteUtils.toHexString(ecKey.getPrivate())
    )

}