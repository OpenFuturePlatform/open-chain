package io.openfuture.chain.core.model.entity.dictionary

import io.openfuture.chain.core.model.entity.base.Dictionary

enum class VoteType(private val id: Int) : Dictionary {

    FOR(1),
    AGAINST(2);

    override fun getId(): Int = id

}