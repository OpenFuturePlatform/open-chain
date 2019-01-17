package io.openfuture.chain.rpc.domain.vote

import io.openfuture.chain.core.model.entity.delegate.ViewDelegate

data class VotesResponse(
    val address: String,
    val publicKey: String,
    val nodeId: String,
    val rating: Long,
    val votesCount: Long,
    val timestamp: Long,
    val recalled: Boolean
) {

    constructor(viewDelegate: ViewDelegate, voteTimestamp: Long, recalled: Boolean) : this(
        viewDelegate.address,
        viewDelegate.publicKey,
        viewDelegate.nodeId,
        viewDelegate.rating,
        viewDelegate.votesCount,
        voteTimestamp,
        recalled
    )

}