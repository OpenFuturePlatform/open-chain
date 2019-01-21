package io.openfuture.chain.rpc.domain

import io.openfuture.chain.core.model.entity.Delegate

data class DelegateResponse(
    val address: String,
    val publicKey: String,
    val nodeId: String
) {

    constructor(delegate: Delegate) : this(
        delegate.address,
        delegate.publicKey,
        delegate.publicKey //todo remove for from
    )

}