package io.openfuture.chain.rpc.domain.node

data class NetworkInfo(
    val interfaceName: String,
    val addresses: List<String>
)