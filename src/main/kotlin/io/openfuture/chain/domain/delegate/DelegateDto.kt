package io.openfuture.chain.domain.delegate

import io.openfuture.chain.entity.Delegate

data class DelegateDto(
        val username: String,
        val address: String,
        val publicKey: String,
        val rating: Int = 0
) {

    constructor(delegate: Delegate) : this(
            delegate.username,
            delegate.address,
            delegate.publicKey
    )

}