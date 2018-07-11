package io.openfuture.chain.domain.stakeholder

import io.openfuture.chain.entity.Stakeholder

class StakeholderDto(
    val address: String,
    val publicKey: String
) {

    constructor(stakeholder: Stakeholder) : this(
        stakeholder.address,
        stakeholder.publicKey
    )

}