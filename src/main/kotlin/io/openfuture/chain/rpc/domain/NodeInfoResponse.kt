package io.openfuture.chain.rpc.domain

data class NodeInfoResponse(
    val publicKey: String,
    val host: String,
    val port: Int
)