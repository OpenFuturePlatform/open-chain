package io.openfuture.chain.domain.delegate

import io.openfuture.chain.network.domain.NetworkAddress

data class DelegateDto(
    val key: String,
    val address: String,
    val networkAddress: NetworkAddress
)