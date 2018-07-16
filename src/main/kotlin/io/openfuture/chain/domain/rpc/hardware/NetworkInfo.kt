package io.openfuture.chain.domain.rpc.hardware

data class NetworkInfo(
    val interfaceName: String,
    val addresses: List<String>
)