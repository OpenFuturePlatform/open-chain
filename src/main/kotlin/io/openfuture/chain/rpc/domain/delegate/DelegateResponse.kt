package io.openfuture.chain.rpc.domain.delegate

import io.openfuture.chain.core.model.entity.state.DelegateState

data class DelegateResponse(
    val address: String,
    val publicKey: String
) {

    constructor(state: DelegateState) : this(
        state.walletAddress,
        state.address
    )

}