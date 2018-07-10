package io.openfuture.chain.domain.delegate

import io.openfuture.chain.entity.account.Stakeholder

data class StakeholderDto(
        val username: String,
        val address: String,
        val publicKey: String,
        val rating: Int = 0,
        val isDelegate: Boolean = false
) {

    constructor(stakeholder: Stakeholder) : this(
            stakeholder.username,
            stakeholder.address,
            stakeholder.publicKey
    )

}