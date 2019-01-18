package io.openfuture.chain.rpc.domain.view

data class ViewDelegateResponse(
    val address: String,
    val publicKey: String,
    val nodeId: String,
    val rating: Long,
    val votesCount: Int,
    val timestamp: Long
)