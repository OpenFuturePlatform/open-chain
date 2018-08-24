package io.openfuture.chain.rpc.domain

import io.openfuture.chain.core.model.entity.Delegate

data class DelegateResponse(
    val address: String,
    val publicKey: String
) {

    constructor(delegate: Delegate) : this(
        delegate.address,
        delegate.publicKey
    )

}