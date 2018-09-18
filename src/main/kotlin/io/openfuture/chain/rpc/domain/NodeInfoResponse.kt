package io.openfuture.chain.rpc.domain

data class NodeInfoResponse(
    val publicKey: String,
    val nodeId: String,
    val host: String,
    val port: Int
)