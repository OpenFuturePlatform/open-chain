package io.openfuture.chain.rpc.domain.view

import io.openfuture.chain.core.model.entity.state.DelegateState

data class ViewDelegateResponse(
    val address: String,
    val publicKey: String,
    val nodeId: String,
    val rating: Long,
    val votesCount: Int,
    val timestamp: Long
) {

    constructor(state: DelegateState) : this(
        state.payload.data.walletAddress,
        "", // state.publicKey,
        state.address,
        state.st,
        state.payload.data.ownVotes.size,
        state.payload.data.registrationDate
    )

}