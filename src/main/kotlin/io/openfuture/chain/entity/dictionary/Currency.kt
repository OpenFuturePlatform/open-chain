package io.openfuture.chain.entity.dictionary

import io.openfuture.chain.entity.base.Dictionary

enum class TransactionType(
        private val id: Int,
        private val value: String
) : Dictionary {

    VOTE(1, "vote");

    override fun getId(): Int = id

    fun getValue(): String = value

}