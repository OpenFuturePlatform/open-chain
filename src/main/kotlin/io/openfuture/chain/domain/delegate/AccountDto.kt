package io.openfuture.chain.domain.delegate

import io.openfuture.chain.entity.account.Account

data class AccountDto(
        val username: String,
        val address: String,
        val publicKey: String,
        val rating: Int = 0,
        val isDelegate: Boolean = false
) {

    constructor(account: Account) : this(
            account.username,
            account.address,
            account.publicKey
    )

}