package io.openfuture.chain.domain.delegate

import io.openfuture.chain.network.domain.NetworkAddress

data class DelegateDto(
    val publicKey: String,
    val networkAddress: NetworkAddress
)