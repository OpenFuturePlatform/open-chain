package io.openfuture.chain.entity.dictionary

import io.openfuture.chain.entity.base.Dictionary

enum class VoteType(
        private val id: Int
) : Dictionary {

    FOR(1),
    AGAINST(2);

    override fun getId(): Int = id

}