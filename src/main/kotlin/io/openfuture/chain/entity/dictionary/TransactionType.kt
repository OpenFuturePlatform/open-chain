package io.openfuture.chain.entity.dictionary

import io.openfuture.chain.entity.base.Dictionary

enum class TransactionType(
        private val id: Int
) : Dictionary {

    VOTE(1),
    DELEGATE_REGISTRATION(2);

    override fun getId(): Int = id

}