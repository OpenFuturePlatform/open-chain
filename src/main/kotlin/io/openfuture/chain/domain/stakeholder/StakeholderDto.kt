package io.openfuture.chain.domain.stakeholder

import io.openfuture.chain.entity.account.Stakeholder

open class StakeholderDto(
        val username: String,
        val address: String,
        val publicKey: String
) {

    constructor(stakeholder: Stakeholder) : this(
            stakeholder.username,
            stakeholder.address,
            stakeholder.publicKey
    )

}