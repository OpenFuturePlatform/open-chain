package io.openfuture.chain.domain.delegate

import io.openfuture.chain.entity.peer.Delegate


class DelegateDto(
    val delegateInfo: DelegateNetworkInfo,
    val rating: Int = 0
) {

    constructor(delegate: Delegate) : this(
        DelegateNetworkInfo(delegate.host, delegate.port),
        delegate.rating
    )

}