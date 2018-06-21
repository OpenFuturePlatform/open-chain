package io.openfuture.chain.domain.hardware

data class NetworkInfo(
        val interfaceName: String,
        val addresses: List<String>
)