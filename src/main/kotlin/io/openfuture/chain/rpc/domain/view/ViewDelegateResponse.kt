package io.openfuture.chain.rpc.domain.view

import io.openfuture.chain.core.model.entity.delegate.ViewDelegate

data class ViewDelegateResponse(
    val address: String,
    val publicKey: String,
    val nodeId: String,
    val rating: Long,
    val votesCount: Long,
    val timestamp: Long
) {

    constructor(delegate: ViewDelegate) : this(
        delegate.address,
        delegate.publicKey,
        delegate.nodeId,
        delegate.rating,
        delegate.votesCount,
        delegate.registrationDate
    )

}