package io.openfuture.chain.rpc.domain.delegate

import io.openfuture.chain.core.model.entity.state.DelegateState

data class ViewDelegateResponse(
    val address: String,
    val delegateKey: String,
    val rating: Long,
    val votesCount: Int,
    val timestamp: Long
) {

    constructor(state: DelegateState, votesCount: Int) : this(
        state.walletAddress,
        state.address,
        state.rating,
        votesCount,
        state.createDate
    )

}