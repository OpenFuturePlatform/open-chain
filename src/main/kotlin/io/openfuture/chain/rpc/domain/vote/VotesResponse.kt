package io.openfuture.chain.rpc.domain.vote

data class VotesResponse(
    val address: String,
    val delegateKey: String,
    val rating: Long,
    val votesCount: Int,
    val timestamp: Long,
    val recalled: Boolean
)