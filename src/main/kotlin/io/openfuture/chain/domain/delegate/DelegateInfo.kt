package io.openfuture.chain.domain.delegate

import io.openfuture.chain.network.domain.NetworkAddress

data class DelegateInfo(
    val networkAddress: NetworkAddress
) {

    constructor(host: String, port: Int) : this (
        NetworkAddress(host, port)
    )

}