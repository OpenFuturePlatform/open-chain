package io.openfuture.chain.rpc.domain.view

data class ViewDelegateResponse(
    val address: String,
    val publicKey: String,  // todo rename delegateKey
    val nodeId: String, // todo remove for front end
    val rating: Long,
    val votesCount: Int,
    val timestamp: Long
)