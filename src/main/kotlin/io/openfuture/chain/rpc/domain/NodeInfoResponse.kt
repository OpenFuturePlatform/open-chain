package io.openfuture.chain.rpc.domain

data class NodeInfoResponse(
    val publicKey: String,
    val nodeId: String, //todo delete for front end
    val host: String,
    val port: Int
)