package io.openfuture.chain.domain.node

import io.openfuture.chain.entity.peer.Delegate

class DelegateDto(
    networkId: String,
    host: String,
    port: Int,
    val rating: Int = 0
) : PeerDto(networkId, host, port) {

    constructor(delegate: Delegate) : this(
        delegate.networkId,
        delegate.host,
        delegate.port,
        delegate.rating
    )

}