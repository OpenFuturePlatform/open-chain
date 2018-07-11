package io.openfuture.chain.domain.stakeholder

import io.openfuture.chain.entity.account.Delegate

class DelegateDto(
    username: String,
    address: String,
    publicKey: String,
    val rating: Int = 0
) : StakeholderDto(username, address, publicKey) {

    constructor(delegate: Delegate) : this(
        delegate.username,
        delegate.address,
        delegate.publicKey,
        delegate.rating
    )

}