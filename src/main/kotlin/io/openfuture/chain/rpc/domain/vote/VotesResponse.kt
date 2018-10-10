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

    constructor(delegate: ViewDelegate, voteTimestamp: Long, recalled: Boolean) : this(
        delegate.address,
        delegate.publicKey,
        delegate.nodeId,
        delegate.rating,
        delegate.votesCount,
        voteTimestamp,
        recalled
    )

}