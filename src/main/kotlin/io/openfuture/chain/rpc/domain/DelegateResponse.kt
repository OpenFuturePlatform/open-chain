package io.openfuture.chain.rpc.domain

import io.openfuture.chain.core.model.entity.Delegate
import io.openfuture.chain.core.model.entity.state.DelegateState

data class DelegateResponse(
    val address: String,
    val publicKey: String,
    val nodeId: String
) {

    constructor(delegate: Delegate) : this(
        delegate.address,
        delegate.publicKey,
        delegate.nodeId
    )

    constructor(state: DelegateState) : this(
        state.payload.data.walletAddress,
        state.address, //todo publivKey
        state.address
    )

}